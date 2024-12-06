package it.gov.pagopa.bizpmingestion.service.impl;

import it.gov.pagopa.bizpmingestion.entity.cosmos.execution.BizEventsPMIngestionExecution;
import it.gov.pagopa.bizpmingestion.entity.pm.PPTransaction;
import it.gov.pagopa.bizpmingestion.enumeration.PMExtractionType;
import it.gov.pagopa.bizpmingestion.model.ExtractionResponse;
import it.gov.pagopa.bizpmingestion.repository.PPTransactionRepository;
import it.gov.pagopa.bizpmingestion.service.IPMExtractionService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.gov.pagopa.bizpmingestion.specification.TransactionSpecifications;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@EnableAsync
@Service
@Slf4j
public class PMExtractionService implements IPMExtractionService {

  @Autowired private SliceService sliceService;

  @Value(value = "${threads.number}")
  private String threadsNumber;

  @Autowired private PPTransactionRepository ppTransactionRepository;
  @Autowired private DayService dayService;

  @Override
  @Transactional
  public ExtractionResponse pmDataExtraction(
      String dateFrom, String dateTo, List<String> taxCodes, PMExtractionType pmExtractionType) {
    log.info("Invoked PMIngestion from {} to {}", dateFrom, dateTo);

    //    dayService.computeDay(LocalDate.parse(dateFrom), LocalDate.parse(dateTo), taxCodes);

    dayService.computeMultipleDays(dateFrom, dateTo, taxCodes);

    return ExtractionResponse.builder().build();

  }


}
