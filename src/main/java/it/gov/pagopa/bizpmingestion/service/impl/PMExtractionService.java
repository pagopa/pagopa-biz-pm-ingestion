package it.gov.pagopa.bizpmingestion.service.impl;

import it.gov.pagopa.bizpmingestion.entity.cosmos.execution.BizEventsPMIngestionExecution;
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
import it.gov.pagopa.bizpmingestion.repository.PMIngestionExecutionRepository;
import it.gov.pagopa.bizpmingestion.repository.PPTransactionRepository;
import it.gov.pagopa.bizpmingestion.service.IPMEventToViewService;
import it.gov.pagopa.bizpmingestion.service.IPMExtractionService;
import it.gov.pagopa.bizpmingestion.specification.BPayExtractionSpec;
import it.gov.pagopa.bizpmingestion.specification.CardExtractionSpec;
import it.gov.pagopa.bizpmingestion.specification.PayPalExtractionSpec;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

@EnableAsync
@Service
@Slf4j
public class PMExtractionService implements IPMExtractionService {

    private static final String LOG_BASE_HEADER_INFO = "[ClassMethod: %s] - [MethodParamsToLog: %s]";

    private final ModelMapper modelMapper;
    private final PPTransactionRepository ppTransactionRepository;
    private final BizEventsViewGeneralRepository bizEventsViewGeneralRepository;
    private final BizEventsViewCartRepository bizEventsViewCartRepository;
    private final BizEventsViewUserRepository bizEventsViewUserRepository;
    private final PMIngestionExecutionRepository pmIngestionExecutionRepository;
    private final IPMEventToViewService pmEventToViewService;
    
    private final Object lock = new Object();
    
    @Autowired
    public PMExtractionService(ModelMapper modelMapper, PPTransactionRepository ppTransactionRepository, 
    		BizEventsViewGeneralRepository bizEventsViewGeneralRepository, BizEventsViewCartRepository bizEventsViewCartRepository, 
    		BizEventsViewUserRepository bizEventsViewUserRepository, PMIngestionExecutionRepository pmIngestionExecutionRepository, 
    		IPMEventToViewService pmEventToViewService) {
        this.modelMapper = modelMapper;
        this.ppTransactionRepository = ppTransactionRepository;
        this.bizEventsViewGeneralRepository = bizEventsViewGeneralRepository;
        this.bizEventsViewCartRepository = bizEventsViewCartRepository;
        this.bizEventsViewUserRepository = bizEventsViewUserRepository;
        this.pmIngestionExecutionRepository = pmIngestionExecutionRepository;
        this.pmEventToViewService = pmEventToViewService;
    }


    @Override
    @Transactional
    public ResponseEntity<Void> pmDataExtraction(String dateFrom, String dateTo, List<String> taxCodes, PMExtractionType pmExtractionType) {
    	
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
        
        processDataAsync(ppTrList, paymentMethodType, pmIngestionExec);
        
        return ResponseEntity.ok().build();
    }
    
    @Async
    public void processDataAsync(List<PPTransaction> ppTrList, PaymentMethodType paymentMethodType, BizEventsPMIngestionExecution pmIngestionExec) {
        
    	try {
    
    		List<Long> skippedId = Collections.synchronizedList(new ArrayList<>());
    		
    		synchronized (lock) {
                pmIngestionExec.setStatus("DONE");
            }
            
        	var pmEventList = ppTrList.stream()
                    .map(ppTransaction -> modelMapper.map(ppTransaction, PMEvent.class))
                    .toList();

            int importedEventsCounter = pmEventList.parallelStream()
                    .map(pmEvent -> {
                        try {
                        	
                        	PMEventPaymentDetail pmEventPaymentDetail = Optional.ofNullable(pmEvent.getPaymentDetailList())
                        	        .orElse(Collections.emptyList())
                        	        .stream()
                        	        .max(Comparator.comparing(PMEventPaymentDetail::getImporto))
                        	        .orElseThrow();

                            PMEventToViewResult result = pmEventToViewService.mapPMEventToView(pmEvent, pmEventPaymentDetail, paymentMethodType);
                            if (result != null) {
                                bizEventsViewGeneralRepository.save(result.getGeneralView());
                                bizEventsViewCartRepository.save(result.getCartView());
                                bizEventsViewUserRepository.saveAll(result.getUserViewList());
                                return 1;
                            }
                            return 0;
                        } catch (Exception e) {
                        	synchronized (lock) {
                                pmIngestionExec.setStatus("DONE WITH SKIP");
                                skippedId.add(pmEvent.getPkTransactionId());
                            }
                            log.error(String.format(LOG_BASE_HEADER_INFO, "processDataAsync", "[processId="+pmIngestionExec.getId()+"] - Error importing PM event with id=" + pmEvent.getPkTransactionId()
                                    + " (err desc = " + e.getMessage() + ")"), e);
                            return 0;
                        }
                    })
                    .reduce(Integer::sum)
                    .orElse(-1);
            
            synchronized (lock) {
                pmIngestionExec.setNumRecordIngested(importedEventsCounter);
                pmIngestionExec.setSkippedID(skippedId);
            }  

        } catch (Exception e) {
        	synchronized (lock) {
                pmIngestionExec.setStatus("FAILED");
            }
            log.error(String.format(LOG_BASE_HEADER_INFO, "processDataAsync", "[processId="+pmIngestionExec.getId()+"] - Error during asynchronous processing: " + e.getMessage()));
        } finally {          
        	synchronized (lock) {
        		pmIngestionExec.setEndTime(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT).format(LocalDateTime.now()));
                pmIngestionExecutionRepository.save(pmIngestionExec);
            }
        }
    }

}
