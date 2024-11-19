package it.gov.pagopa.bizpmingestion.service.impl;

import it.gov.pagopa.bizpmingestion.entity.cosmos.execution.BizEventsPMIngestionExecution;
import it.gov.pagopa.bizpmingestion.entity.pm.PPTransaction;
import it.gov.pagopa.bizpmingestion.enumeration.PMExtractionType;
import it.gov.pagopa.bizpmingestion.enumeration.PaymentMethodType;
import it.gov.pagopa.bizpmingestion.exception.AppError;
import it.gov.pagopa.bizpmingestion.exception.AppException;
import it.gov.pagopa.bizpmingestion.model.ExtractionResponse;
import it.gov.pagopa.bizpmingestion.model.pm.PMEvent;
import it.gov.pagopa.bizpmingestion.repository.PPTransactionRepository;
import it.gov.pagopa.bizpmingestion.service.IPMExtractionService;
import it.gov.pagopa.bizpmingestion.specification.BPayExtractionSpec;
import it.gov.pagopa.bizpmingestion.specification.CardExtractionSpec;
import it.gov.pagopa.bizpmingestion.specification.PayPalExtractionSpec;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@EnableAsync
@Service
@Slf4j
public class PMExtractionService implements IPMExtractionService {

    private static final String LOG_BASE_HEADER_INFO = "[ClassMethod: %s] - [MethodParamsToLog: %s]";

    private final ModelMapper modelMapper;
    private final PPTransactionRepository ppTransactionRepository;

    @Autowired
    AsyncService asyncService;


    @Autowired
    public PMExtractionService(ModelMapper modelMapper, PPTransactionRepository ppTransactionRepository) {
        this.modelMapper = modelMapper;
        this.ppTransactionRepository = ppTransactionRepository;
    }


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

        PaymentMethodType paymentMethodType;
        Specification<PPTransaction> spec = switch (pmExtractionType) {
            case CARD -> {
                paymentMethodType = PaymentMethodType.CP;
                yield new CardExtractionSpec(dateFrom, dateTo, taxCodes);
            }
            case BPAY -> {
                paymentMethodType = PaymentMethodType.JIF;
                yield new BPayExtractionSpec(dateFrom, dateTo, taxCodes);
            }
            case PAYPAL -> {
                paymentMethodType = PaymentMethodType.PPAL;
                yield new PayPalExtractionSpec(dateFrom, dateTo, taxCodes);
            }
            default -> throw new AppException(AppError.BAD_REQUEST,
                    "Invalid PM extraction type [pmExtractionType=" + pmExtractionType + "]");
        };

        List<PPTransaction> ppTrList = ppTransactionRepository.findAll(Specification.where(spec));

        pmIngestionExec.setNumRecordFound(ppTrList.size());

        var pmEventList = ppTrList.stream()
                .map(ppTransaction -> modelMapper.map(ppTransaction, PMEvent.class))
                .toList();

        asyncService.processDataAsync(pmEventList, paymentMethodType, pmIngestionExec);

        return ExtractionResponse.builder()
                .elements(ppTrList.size())
                .build();
    }


}
