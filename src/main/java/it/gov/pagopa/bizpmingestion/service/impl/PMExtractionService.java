package it.gov.pagopa.bizpmingestion.service.impl;

import it.gov.pagopa.bizpmingestion.entity.TransactionMergeDTO;
import it.gov.pagopa.bizpmingestion.entity.cosmos.execution.BizEventsPMIngestionExecution;
import it.gov.pagopa.bizpmingestion.entity.pm.PPTransaction;
import it.gov.pagopa.bizpmingestion.enumeration.PMExtractionType;
import it.gov.pagopa.bizpmingestion.enumeration.PaymentMethodType;
import it.gov.pagopa.bizpmingestion.exception.AppError;
import it.gov.pagopa.bizpmingestion.exception.AppException;
import it.gov.pagopa.bizpmingestion.model.ExtractionResponse;
import it.gov.pagopa.bizpmingestion.model.pm.PMEvent;
import it.gov.pagopa.bizpmingestion.repository.MyTransactionRepository;
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

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@EnableAsync
@Service
@Slf4j
public class PMExtractionService implements IPMExtractionService {

    private static final String LOG_BASE_HEADER_INFO = "[ClassMethod: %s] - [MethodParamsToLog: %s]";

    private final ModelMapper modelMapper;
    private final PPTransactionRepository ppTransactionRepository;

    @Autowired
    private MyTransactionRepository myTransactionRepository;

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

        Map<Long, TransactionMergeDTO> ppTrList = new HashMap<>();
        if (pmExtractionType.equals(PMExtractionType.CARD)) {
            Timestamp from = Timestamp.valueOf(LocalDate.parse(dateFrom, DateTimeFormatter.ISO_DATE).atStartOfDay());
            Timestamp to = Timestamp.valueOf(LocalDate.parse(dateTo, DateTimeFormatter.ISO_DATE).atStartOfDay());
            myTransactionRepository.findTransactionsByCard(from, to)
                    .forEach(transactionMergeDTO -> {
                        if (ppTrList.containsKey(transactionMergeDTO.getTransactionId())) {
                            if (transactionMergeDTO.getAmount() != null) {

                                Long amount = ppTrList.get(transactionMergeDTO.getTransactionId()).getAmount();
                                if (transactionMergeDTO.getAmount() > amount) {
                                    ppTrList.put(transactionMergeDTO.getTransactionId(), transactionMergeDTO);
                                }
                            }
                        } else {
                            ppTrList.put(transactionMergeDTO.getTransactionId(), transactionMergeDTO);
                        }
                    });
        }
        List<PMEvent> pmEventList = new ArrayList<>();
        for (var elem : ppTrList.entrySet()) {
            var mapped = modelMapper.map(elem, PMEvent.class);
            pmEventList.add(mapped);
        }

        asyncService.processDataAsync(pmEventList, paymentMethodType, pmIngestionExec);

        return ExtractionResponse.builder()
                .elements(ppTrList.size())
                .build();
    }


}
