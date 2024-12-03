package it.gov.pagopa.bizpmingestion.service.impl;

import it.gov.pagopa.bizpmingestion.entity.cosmos.execution.BizEventsPMIngestionExecution;
import it.gov.pagopa.bizpmingestion.entity.pm.*;
import it.gov.pagopa.bizpmingestion.enumeration.PMExtractionType;
import it.gov.pagopa.bizpmingestion.enumeration.PaymentMethodType;
import it.gov.pagopa.bizpmingestion.exception.AppError;
import it.gov.pagopa.bizpmingestion.exception.AppException;
import it.gov.pagopa.bizpmingestion.model.ExtractionResponse;
import it.gov.pagopa.bizpmingestion.model.pm.PMEvent;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventPayPal;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventPaymentDetail;
import it.gov.pagopa.bizpmingestion.repository.PPPspRepository;
import it.gov.pagopa.bizpmingestion.repository.PPTransactionRepository;
import it.gov.pagopa.bizpmingestion.service.IPMExtractionService;
import it.gov.pagopa.bizpmingestion.specification.PmExtractionSpec;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.spi.MappingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@EnableAsync
@Service
@Slf4j
public class PMExtractionService implements IPMExtractionService {


    @Autowired
    private AsyncService asyncService;



    @Override
    @Transactional
    public ExtractionResponse pmDataExtraction(String dateFrom, String dateTo, List<String> taxCodes, PMExtractionType pmExtractionType) {

        BizEventsPMIngestionExecution pmIngestionExec = BizEventsPMIngestionExecution.builder()
                .id(UUID.randomUUID().toString())
                .startTime(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT).format(LocalDateTime.now()))
                .dateFrom(dateFrom)
                .dateTo(dateTo)
                .taxCodesFilter(taxCodes)
                .extractionType(pmExtractionType)
                .build();

        Specification<PPTransaction> spec = new PmExtractionSpec(dateFrom, dateTo, taxCodes);

        asyncService.processDataAsync(spec, pmIngestionExec);

        return ExtractionResponse.builder()
                .elements(-1)
                .build();
    }


}
