package it.gov.pagopa.bizpmingestion.service.impl;

import it.gov.pagopa.bizpmingestion.entity.cosmos.execution.BizEventsPMIngestionExecution;
import it.gov.pagopa.bizpmingestion.entity.pm.PPTransaction;
import it.gov.pagopa.bizpmingestion.repository.PPTransactionRepository;
import it.gov.pagopa.bizpmingestion.specification.TransactionSpecifications;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@EnableAsync
@Service
@Slf4j
public class DayService {

  @Value(value = "${threads.number}")
  private String threadsNumber;

  @Autowired private PPTransactionRepository ppTransactionRepository;

  @Autowired private SliceService sliceService;

  @Async
  public void computeMultipleDays(String dateFrom, String dateTo, List<String> taxCodes) {
    LocalDate startDate = LocalDate.parse(dateFrom);

    LocalDate dayTo = LocalDate.parse(dateTo);

    while (dayTo.isAfter(startDate)) {
      LocalDate dayFrom = dayTo.minusDays(1);
      computeDay(dayFrom, dayTo, taxCodes);
      dayTo = dayFrom;
    }
  }

  public void computeDay(LocalDate dateFrom, LocalDate dateTo, List<String> taxCodes) {
    long startTime = System.currentTimeMillis();

    ExecutorService executor = Executors.newFixedThreadPool(Integer.parseInt(threadsNumber));

    int slicesNumber = getSlicesNumber(dateFrom, dateTo);

    log.info("PMIngestion Computing of the day with {} slices [Day {}]", slicesNumber, dateFrom);

    List<LocalDateTime> fractions = generateDateSlices(dateFrom, dateTo, slicesNumber);

    List<CompletableFuture<BizEventsPMIngestionExecution>> futures = new ArrayList<>();
    for (int i = 0; i < fractions.size() - 1; i++) {
      LocalDateTime dateFromSlice = fractions.get(i);
      LocalDateTime dateToSlice = fractions.get(i + 1);
      BizEventsPMIngestionExecution pmIngestionExec =
          BizEventsPMIngestionExecution.builder()
              .requestId(UUID.randomUUID().toString())
              .startTime(
                  DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT)
                      .format(LocalDateTime.now()))
              .dateFrom(dateFromSlice.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
              .dateTo(dateToSlice.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
              .taxCodesFilter(taxCodes)
              //              .extractionType(pmExtractionType)
              .build();

      Specification<PPTransaction> spec =
          new TransactionSpecifications()
              .getFilteredTransactions(dateFromSlice, dateToSlice, taxCodes);

      int finalI = i;
      var future =
          CompletableFuture.supplyAsync(
              () -> sliceService.computeSlice(spec, pmIngestionExec, finalI), executor);

      futures.add(future);
    }

    // CompletableFuture che si completa quando tutti i task sono terminati
    CompletableFuture<Void> allDone =
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

    allDone
        .thenAccept(
            (v) -> {
              List<BizEventsPMIngestionExecution> slices =
                  futures.stream().map(CompletableFuture::join).toList();

              long endTime = System.currentTimeMillis();
              long totalTimeMillis = (endTime - startTime);

              long hours = totalTimeMillis / 3600000; // Ore intere
              long remainingMillisAfterHours =
                  totalTimeMillis % 3600000; // Millisecondi rimanenti dopo aver calcolato le ore

              long minutes = remainingMillisAfterHours / 60000; // Minuti interi
              long remainingMillisAfterMinutes =
                  remainingMillisAfterHours
                      % 60000; // Millisecondi rimanenti dopo aver calcolato i minuti

              long seconds = remainingMillisAfterMinutes / 1000; // Secondi interi

              log.info(
                  "Completed PMIngestion all of the {} slices after {} h {} m {} s - ingested {}/{} records [Day {}]",
                  slicesNumber,
                  hours,
                  minutes,
                  seconds,
                  slices.stream()
                      .map(BizEventsPMIngestionExecution::getNumRecordIngested)
                      .reduce(Integer::sum)
                      .orElse(-1),
                  slices.stream()
                      .map(BizEventsPMIngestionExecution::getNumRecordFound)
                      .reduce(Integer::sum)
                      .orElse(-1),
                  dateFrom);
            })
        .thenRun(executor::shutdown)
        .exceptionally(
            (ex) -> {
              executor.shutdown();
              log.error("Error PMIngestion [Day {}]", dateFrom, ex);
              return null;
            })
        .join();
  }

  private int getSlicesNumber(LocalDate dateFrom, LocalDate dateTo) {
    long count =
        ppTransactionRepository.count(TransactionSpecifications.countTransactions(
                        dateFrom.atStartOfDay(), dateTo.atStartOfDay()));

    int slicesNumber = Math.toIntExact(count / (2 * 2500));
    slicesNumber = Math.max(10, slicesNumber);
    slicesNumber = Math.min(slicesNumber, 50);
    return slicesNumber;
  }

  public static List<LocalDateTime> generateDateSlices(
      LocalDate startDate, LocalDate endDate, int n) {

    // Verifica che il numero di partizioni sia valido
    if (n <= 0) {
      throw new IllegalArgumentException("Il numero di partizioni deve essere maggiore di zero.");
    }
    if (!startDate.isBefore(endDate) && !startDate.isEqual(endDate)) {
      throw new IllegalArgumentException(
          "La data iniziale deve essere precedente o uguale alla data finale.");
    }

    // Convertire le date a LocalDateTime UTC
    LocalDateTime startDateTime =
        startDate.atStartOfDay().atOffset(ZoneOffset.UTC).toLocalDateTime();
    LocalDateTime endDateTime = endDate.atStartOfDay().atOffset(ZoneOffset.UTC).toLocalDateTime();

    // Calcolare la durata totale in secondi
    long totalSeconds = ChronoUnit.SECONDS.between(startDateTime, endDateTime);

    // Calcolare la durata di ogni partizione
    long sliceSeconds = totalSeconds / n;

    // Generare le partizioni
    List<LocalDateTime> slices = new ArrayList<>();
    for (int i = 0; i < n; i++) {
      slices.add(startDateTime.plusSeconds(sliceSeconds * i));
    }
    slices.add(endDateTime);

    return slices;
  }
}