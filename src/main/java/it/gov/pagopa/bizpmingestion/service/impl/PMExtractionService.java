package it.gov.pagopa.bizpmingestion.service.impl;

import it.gov.pagopa.bizpmingestion.entity.pm.PPTransaction;
import it.gov.pagopa.bizpmingestion.enumeration.PMExtractionType;
import it.gov.pagopa.bizpmingestion.enumeration.PaymentMethodType;
import it.gov.pagopa.bizpmingestion.exception.AppError;
import it.gov.pagopa.bizpmingestion.exception.AppException;
import it.gov.pagopa.bizpmingestion.model.pm.PMEvent;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventPaymentDetail;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventToViewResult;
import it.gov.pagopa.bizpmingestion.repository.BizEventsViewCartRepository;
import it.gov.pagopa.bizpmingestion.repository.BizEventsViewGeneralRepository;
import it.gov.pagopa.bizpmingestion.repository.BizEventsViewUserRepository;
import it.gov.pagopa.bizpmingestion.repository.PPTransactionRepository;
import it.gov.pagopa.bizpmingestion.service.IPMEventToViewService;
import it.gov.pagopa.bizpmingestion.service.IPMExtractionService;
import it.gov.pagopa.bizpmingestion.specification.BPayExtractionSpec;
import it.gov.pagopa.bizpmingestion.specification.CardExtractionSpec;
import it.gov.pagopa.bizpmingestion.specification.PayPalExtractionSpec;
import it.gov.pagopa.bizpmingestion.util.CommonUtility;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


@Service
@Slf4j
public class PMExtractionService implements IPMExtractionService {

    private static final String LOG_BASE_HEADER_INFO = "[ClassMethod: %s] - [MethodParamsToLog: %s]";
    private static final String METHOD = "pmDataExtraction";

    private final ModelMapper modelMapper;
    private final PPTransactionRepository ppTransactionRepository;
    private final BizEventsViewGeneralRepository bizEventsViewGeneralRepository;
    private final BizEventsViewCartRepository bizEventsViewCartRepository;
    private final BizEventsViewUserRepository bizEventsViewUserRepository;
    private final IPMEventToViewService pmEventToViewService;
    private final TransactionService transactionService;

    @Autowired
    public PMExtractionService(ModelMapper modelMapper, PPTransactionRepository ppTransactionRepository, BizEventsViewGeneralRepository bizEventsViewGeneralRepository, BizEventsViewCartRepository bizEventsViewCartRepository, BizEventsViewUserRepository bizEventsViewUserRepository, IPMEventToViewService pmEventToViewService, TransactionService transactionService) {
        this.modelMapper = modelMapper;
        this.ppTransactionRepository = ppTransactionRepository;
        this.bizEventsViewGeneralRepository = bizEventsViewGeneralRepository;
        this.bizEventsViewCartRepository = bizEventsViewCartRepository;
        this.bizEventsViewUserRepository = bizEventsViewUserRepository;
        this.pmEventToViewService = pmEventToViewService;
        this.transactionService = transactionService;
    }


    @Override
    public void pmDataExtraction(String dateFrom, String dateTo, List<String> taxCodes, PMExtractionType pmExtractionType) {
        log.info(String.format(LOG_BASE_HEADER_INFO, METHOD, CommonUtility.sanitize(pmExtractionType.toString()) + " type data extraction running at " + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now())));

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
        log.info(String.format(LOG_BASE_HEADER_INFO, METHOD, CommonUtility.sanitize(pmExtractionType.toString()) + " type data extraction info: Found n. " + ppTrList.size()
                + " transactions to save on Cosmos DB."
                + " Setted Filters: dateFrom=" + CommonUtility.sanitize(dateFrom) + ", dateFrom=" + CommonUtility.sanitize(dateTo) + ", taxCodes=" + CommonUtility.sanitize(taxCodes.toString()) + "."
                + " Started at " + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now())));

        var pmEventList = ppTrList.stream()
                .map(ppTransaction -> modelMapper.map(ppTransaction, PMEvent.class))
                .toList();

        long startTime = System.currentTimeMillis();

        int importedEventsCounter = pmEventList.parallelStream()
                .map(pmEvent -> {
                    try {
                        PMEventPaymentDetail pmEventPaymentDetail = pmEvent.getPaymentDetailList()
                                .stream()
                                .max(Comparator.comparing(PMEventPaymentDetail::getImporto))
                                .orElseThrow();
                        log.info("trace[max] {}", System.currentTimeMillis() - startTime);
                        PMEventToViewResult result = pmEventToViewService.mapPMEventToView(pmEvent, pmEventPaymentDetail, paymentMethodType);
                        log.info("trace[mapping] {}", System.currentTimeMillis() - startTime);
                        if (result != null) {
                            bizEventsViewGeneralRepository.save(result.getGeneralView());
                            log.info("trace[save general] {}", System.currentTimeMillis() - startTime);
                            
                            bizEventsViewCartRepository.save(result.getCartView());
                            log.info("trace[save cart] {}", System.currentTimeMillis() - startTime);
                            
                            bizEventsViewUserRepository.saveAll(result.getUserViewList());
                            log.info("trace[save user] {}", System.currentTimeMillis() - startTime);
                            return 1;
                        }
                        return 0;
                    } catch (Exception e) {
                        log.error(String.format(LOG_BASE_HEADER_INFO, METHOD, CommonUtility.sanitize(pmExtractionType.toString()) + " type data extraction info: Error importing PM event with id=" + pmEvent.getPkTransactionId()
                                + " (err desc = " + e.getMessage() + ")"), e);
                        return 0;
                    }
                })
                .reduce(Integer::sum)
                .orElse(-1);
        log.info(String.format(LOG_BASE_HEADER_INFO, METHOD, CommonUtility.sanitize(pmExtractionType.toString()) + " type data extraction info: executed in "+ (System.currentTimeMillis() - startTime) +" Imported n. " + importedEventsCounter
                + " events out of a total of " + ppTrList.size() + "."
                + " Finished at " + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now())));
    }

}
