package it.gov.pagopa.bizpmingestion.service.impl;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.microsoft.azure.functions.annotation.ExponentialBackoffRetry;
import it.gov.pagopa.bizpmingestion.entity.cosmos.execution.BizEventsPMIngestionExecution;
import it.gov.pagopa.bizpmingestion.entity.cosmos.execution.SkippedTransaction;
import it.gov.pagopa.bizpmingestion.entity.pm.PPTransaction;
import it.gov.pagopa.bizpmingestion.model.WrapperObject;
import it.gov.pagopa.bizpmingestion.model.pm.PMEvent;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventPaymentDetail;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventToViewResult;
import it.gov.pagopa.bizpmingestion.repository.*;
import it.gov.pagopa.bizpmingestion.service.IPMEventToViewService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@EnableAsync
@Service
@Slf4j
public class SliceService {

  private static final String LOG_BASE_HEADER_INFO = "[ClassMethod: %s] - [MethodParamsToLog: %s]";
  public static final int PAGE_SIZE = 1000;

  private final BizEventsViewGeneralRepository bizEventsViewGeneralRepository;
  private final BizEventsViewCartRepository bizEventsViewCartRepository;
  private final BizEventsViewUserRepository bizEventsViewUserRepository;
  private final PMIngestionExecutionRepository pmIngestionExecutionRepository;
  private final IPMEventToViewService pmEventToViewService;

  @Autowired private MapperComponent mapperComponent;

  @Autowired private PPTransactionRepository ppTransactionRepository;

  @Autowired
  public SliceService(
      BizEventsViewGeneralRepository bizEventsViewGeneralRepository,
      BizEventsViewCartRepository bizEventsViewCartRepository,
      BizEventsViewUserRepository bizEventsViewUserRepository,
      PMIngestionExecutionRepository pmIngestionExecutionRepository,
      IPMEventToViewService pmEventToViewService) {
    this.bizEventsViewGeneralRepository = bizEventsViewGeneralRepository;
    this.bizEventsViewCartRepository = bizEventsViewCartRepository;
    this.bizEventsViewUserRepository = bizEventsViewUserRepository;
    this.pmIngestionExecutionRepository = pmIngestionExecutionRepository;
    this.pmEventToViewService = pmEventToViewService;
  }

  @Transactional
  public BizEventsPMIngestionExecution computeSlice(
      Specification<PPTransaction> spec,
      BizEventsPMIngestionExecution pmIngestionExec,
      int sliceNumber) {
    try {
      pmIngestionExec.setId(UUID.randomUUID().toString());
      List<PPTransaction> ppTrList = ppTransactionRepository.findAll(spec);

      List<PMEvent> pmEventList =
          ppTrList.stream().map(elem -> mapperComponent.convert(elem)).toList();

      handleEventList(pmEventList, pmIngestionExec, sliceNumber);

    } catch (Exception e) {
      pmIngestionExec.setStatus("FAILED");
      log.error(
          "Error during asynchronous processing. [transactionId={}] ", pmIngestionExec.getId(), e);

    } finally {
      pmIngestionExec.setEndTime(
          DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT)
              .format(LocalDateTime.now()));

      //      pmIngestionExecutionRepository.save(pmIngestionExec);
      log.info(
          "PMIngestion Slice n° {} [{} to {}] Done with status {} - {}/{} records [Day {}]",
          sliceNumber,
          pmIngestionExec.getDateFrom(),
          pmIngestionExec.getDateTo(),
          pmIngestionExec.getStatus(),
          pmIngestionExec.getNumRecordIngested(),
          pmIngestionExec.getNumRecordFound(),
          LocalDate.parse(pmIngestionExec.getDateFrom()));
    }
    return pmIngestionExec;
  }

  private void handleEventList(
      List<PMEvent> pmEventList, BizEventsPMIngestionExecution pmIngestionExec, int sliceNumber) {
    pmIngestionExec.setStatus("ALL_COMPLETED");
    pmIngestionExec.setSliceNumber(sliceNumber);
    pmIngestionExec.setNumRecordIngested(0);
    pmIngestionExec.setNumRecordFound(pmEventList.size());

    List<WrapperObject> wrappers =
        pmEventList.parallelStream()
            .map(
                pmEvent -> {
                  try {
                    PMEventPaymentDetail pmEventPaymentDetail =
                        Optional.ofNullable(pmEvent.getPaymentDetailList())
                            .orElse(Collections.emptyList())
                            .stream()
                            .findFirst()
                            //
                            // .max(Comparator.comparing(PMEventPaymentDetail::getImporto))
                            .orElseThrow(
                                () ->
                                    new RuntimeException(
                                        pmEvent.getUserFiscalCode()
                                            + " importo null. transactionId="
                                            + pmEvent.getPkTransactionId()));

                    PMEventToViewResult result =
                        pmEventToViewService.mapPMEventToView(pmEvent, pmEventPaymentDetail);
                    if (result != null) {
                      return WrapperObject.builder()
                          .cartView(result.getCartView())
                          .generalView(result.getGeneralView())
                          .userViewList(result.getUserViewList())
                          .build();
                    } else {
                      throw new RuntimeException(
                          "payer and debtor are null. transactionID="
                              + pmEvent.getPkTransactionId());
                    }
                  } catch (Exception e) {
                    pmIngestionExec.setStatus("COMPLETED");
                    return null;
                  }
                })
            .filter(Objects::nonNull)
            .toList();

    var generals = wrappers.stream().map(WrapperObject::getGeneralView).toList();
    var cart = wrappers.stream().map(WrapperObject::getCartView).toList();
    var user = wrappers.stream().map(WrapperObject::getUserViewList).flatMap(List::stream).toList();
    //    var skipped = wrappers.stream().map(WrapperObject::getSkippedTransaction).toList();

    ExecutorService executor = Executors.newFixedThreadPool(3);

    var f1 =
        CompletableFuture.runAsync(
            () -> bulkSave(generals, bizEventsViewGeneralRepository), executor);
    var f2 =
        CompletableFuture.runAsync(() -> bulkSave(cart, bizEventsViewCartRepository), executor);

    var f3 =
        CompletableFuture.runAsync(() -> bulkSave(user, bizEventsViewUserRepository), executor);

    try {
      CompletableFuture.allOf(f1, f2, f3).join();
    } finally {
      executor.shutdown();
    }

    pmIngestionExec.setNumRecordIngested(wrappers.size());
    //    pmIngestionExec.setSkippedID(skipped);
  }

  @ExponentialBackoffRetry(
      maxRetryCount = 3,
      maximumInterval = "00:00:30",
      minimumInterval = "00:00:5")
  private <T> void bulkSave(List<T> generals, CosmosRepository<T, String> repository) {
    int bulkSize = 500;
    for (int i = 0; i < generals.size(); i += bulkSize) {
      int endIndex = Math.min(i + bulkSize, generals.size());
      List<T> bulk = generals.subList(i, endIndex);
      repository.saveAll(bulk);
    }
  }
}
