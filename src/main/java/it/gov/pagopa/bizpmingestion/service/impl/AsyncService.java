package it.gov.pagopa.bizpmingestion.service.impl;

import com.microsoft.azure.functions.annotation.ExponentialBackoffRetry;
import it.gov.pagopa.bizpmingestion.entity.cosmos.execution.BizEventsPMIngestionExecution;
import it.gov.pagopa.bizpmingestion.entity.cosmos.execution.SkippedTransaction;
import it.gov.pagopa.bizpmingestion.entity.pm.PPTransaction;
import it.gov.pagopa.bizpmingestion.model.pm.PMEvent;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventPaymentDetail;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventToViewResult;
import it.gov.pagopa.bizpmingestion.repository.*;
import it.gov.pagopa.bizpmingestion.service.IPMEventToViewService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@EnableAsync
@Service
@Slf4j
public class AsyncService {

    private static final String LOG_BASE_HEADER_INFO = "[ClassMethod: %s] - [MethodParamsToLog: %s]";
    public static final int PAGE_SIZE = 1000;

    private final BizEventsViewGeneralRepository bizEventsViewGeneralRepository;
    private final BizEventsViewCartRepository bizEventsViewCartRepository;
    private final BizEventsViewUserRepository bizEventsViewUserRepository;
    private final PMIngestionExecutionRepository pmIngestionExecutionRepository;
    private final IPMEventToViewService pmEventToViewService;

    @Autowired
    private MapperComponent mapperComponent;


    @Autowired
    private PPTransactionRepository ppTransactionRepository;


    @Autowired
    public AsyncService(BizEventsViewGeneralRepository bizEventsViewGeneralRepository, BizEventsViewCartRepository bizEventsViewCartRepository,
                        BizEventsViewUserRepository bizEventsViewUserRepository, PMIngestionExecutionRepository pmIngestionExecutionRepository,
                        IPMEventToViewService pmEventToViewService) {
        this.bizEventsViewGeneralRepository = bizEventsViewGeneralRepository;
        this.bizEventsViewCartRepository = bizEventsViewCartRepository;
        this.bizEventsViewUserRepository = bizEventsViewUserRepository;
        this.pmIngestionExecutionRepository = pmIngestionExecutionRepository;
        this.pmEventToViewService = pmEventToViewService;
    }

//    @Async
    @Transactional
    public BizEventsPMIngestionExecution processDataAsync(Specification<PPTransaction> spec, BizEventsPMIngestionExecution pmIngestionExec, int sliceNumber) {
        try {
            pmIngestionExec.setId(UUID.randomUUID().toString());
            List<PPTransaction> ppTrList = ppTransactionRepository.findAll(spec);

            List<PMEvent> pmEventList = ppTrList.stream()
                    .map(elem -> mapperComponent.convert(elem))
                    .toList();

            handleEventList(pmEventList, pmIngestionExec, sliceNumber);

        } catch (Exception e) {
            pmIngestionExec.setStatus("FAILED");
            log.error(String.format(LOG_BASE_HEADER_INFO, "processDataAsync", "[processId=" + pmIngestionExec.getId() + "] - Error during asynchronous processing: " + e.getMessage()));
        } finally {
            pmIngestionExec.setEndTime(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT).format(LocalDateTime.now()));

            pmIngestionExecutionRepository.save(pmIngestionExec);
            log.info("Done {}/{} records - Slice n° {} ", pmIngestionExec.getNumRecordIngested(), pmIngestionExec.getNumRecordFound(), sliceNumber);
        }
        return pmIngestionExec;
    }

    private void handleEventList(List<PMEvent> pmEventList, BizEventsPMIngestionExecution pmIngestionExec, int sliceNumber) {
        pmIngestionExec.setStatus("DONE");
        pmIngestionExec.setSliceNumber(sliceNumber);
        pmIngestionExec.setNumRecordIngested(0);
        pmIngestionExec.setNumRecordFound(pmEventList.size());

        List<SkippedTransaction> skippedId = pmEventList.parallelStream()
                .map(pmEvent -> {
                    try {
                        PMEventPaymentDetail pmEventPaymentDetail = Optional.ofNullable(pmEvent.getPaymentDetailList())
                                .orElse(Collections.emptyList())
                                .stream()
                                .findFirst()
//                                .max(Comparator.comparing(PMEventPaymentDetail::getImporto))
                                .orElseThrow(() -> new RuntimeException(pmEvent.getUserFiscalCode() + " importo null. transactionId=" + pmEvent.getPkTransactionId()));

                        PMEventToViewResult result = pmEventToViewService.mapPMEventToView(pmEvent, pmEventPaymentDetail);
                        if (result != null) {
                            saveOnCosmos(result);
                        } else {
                            throw new RuntimeException("payer and debtor are null. transactionID=" + pmEvent.getPkTransactionId());
                        }
                        return null;
                    } catch (Exception e) {
                        pmIngestionExec.setStatus("DONE WITH SKIP");

                        return SkippedTransaction.builder()
                                .transactionId(pmEvent.getPkTransactionId())
                                .cause(e.getMessage())
                                .build();
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        pmIngestionExec.setNumRecordIngested(pmEventList.size() - skippedId.size());
        pmIngestionExec.setSkippedID(skippedId);
    }

    @ExponentialBackoffRetry(maxRetryCount = 3, maximumInterval = "00:00:30", minimumInterval = "00:00:10")
    private void saveOnCosmos(PMEventToViewResult result) {
        bizEventsViewGeneralRepository.save(result.getGeneralView());
        bizEventsViewCartRepository.save(result.getCartView());
        bizEventsViewUserRepository.saveAll(result.getUserViewList());
    }


}
