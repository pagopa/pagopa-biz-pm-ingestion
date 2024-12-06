package it.gov.pagopa.bizpmingestion.service.impl;

import it.gov.pagopa.bizpmingestion.enumeration.PMExtractionType;
import it.gov.pagopa.bizpmingestion.model.ExtractionResponse;
import it.gov.pagopa.bizpmingestion.service.IPMExtractionService;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@EnableAsync
@Service
@Slf4j
public class PMExtractionService implements IPMExtractionService {

  @Autowired private SliceService sliceService;
  @Autowired private DayService dayService;

  @Override
  @Transactional
  public ExtractionResponse pmDataExtraction(
      String dateFrom, String dateTo, List<String> taxCodes, PMExtractionType pmExtractionType) {
    log.info("Start PMIngestion from {} to {}", dateFrom, dateTo);

    //    dayService.computeDay(LocalDate.parse(dateFrom), LocalDate.parse(dateTo), taxCodes);

    dayService.computeMultipleDays(dateFrom, dateTo, taxCodes);

    return ExtractionResponse.builder().build();
  }
}
