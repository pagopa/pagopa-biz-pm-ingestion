package it.gov.pagopa.bizpmingestion.service.impl;

import it.gov.pagopa.bizpmingestion.entity.cosmos.execution.BizEventsPMIngestionExecution;
import it.gov.pagopa.bizpmingestion.entity.pm.PPTransaction;
import it.gov.pagopa.bizpmingestion.enumeration.PaymentMethodType;
import it.gov.pagopa.bizpmingestion.model.pm.PMEvent;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventPaymentDetail;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventToViewResult;
import it.gov.pagopa.bizpmingestion.repository.*;
import it.gov.pagopa.bizpmingestion.service.IPMEventToViewService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@EnableAsync
@Service
@Slf4j
public class AyncService {

    private static final String LOG_BASE_HEADER_INFO = "[ClassMethod: %s] - [MethodParamsToLog: %s]";

    private final ModelMapper modelMapper;
    private final BizEventsViewGeneralRepository bizEventsViewGeneralRepository;
    private final BizEventsViewCartRepository bizEventsViewCartRepository;
    private final BizEventsViewUserRepository bizEventsViewUserRepository;
    private final PMIngestionExecutionRepository pmIngestionExecutionRepository;
    private final IPMEventToViewService pmEventToViewService;

    @Autowired
    public AyncService(ModelMapper modelMapper, PPTransactionRepository ppTransactionRepository,
                       BizEventsViewGeneralRepository bizEventsViewGeneralRepository, BizEventsViewCartRepository bizEventsViewCartRepository,
                       BizEventsViewUserRepository bizEventsViewUserRepository, PMIngestionExecutionRepository pmIngestionExecutionRepository,
                       IPMEventToViewService pmEventToViewService) {
        this.modelMapper = modelMapper;
        this.bizEventsViewGeneralRepository = bizEventsViewGeneralRepository;
        this.bizEventsViewCartRepository = bizEventsViewCartRepository;
        this.bizEventsViewUserRepository = bizEventsViewUserRepository;
        this.pmIngestionExecutionRepository = pmIngestionExecutionRepository;
        this.pmEventToViewService = pmEventToViewService;
    }

    @Async
    public void processDataAsync(List<PPTransaction> ppTrList, PaymentMethodType paymentMethodType, BizEventsPMIngestionExecution pmIngestionExec) {

        try {

            List<Long> skippedId = Collections.synchronizedList(new ArrayList<>());
            pmIngestionExec.setStatus("DONE");

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
                            pmIngestionExec.setStatus("DONE WITH SKIP");
                            skippedId.add(pmEvent.getPkTransactionId());

                            log.error(String.format(LOG_BASE_HEADER_INFO, "processDataAsync", "[processId=" + pmIngestionExec.getId() + "] - Error importing PM event with id=" + pmEvent.getPkTransactionId()
                                    + " (err desc = " + e.getMessage() + ")"), e);
                            return 0;
                        }
                    })
                    .reduce(Integer::sum)
                    .orElse(-1);

            pmIngestionExec.setNumRecordIngested(importedEventsCounter);
            pmIngestionExec.setSkippedID(skippedId);


        } catch (Exception e) {
            pmIngestionExec.setStatus("FAILED");

            log.error(String.format(LOG_BASE_HEADER_INFO, "processDataAsync", "[processId=" + pmIngestionExec.getId() + "] - Error during asynchronous processing: " + e.getMessage()));
        } finally {
            pmIngestionExec.setEndTime(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT).format(LocalDateTime.now()));
            pmIngestionExecutionRepository.save(pmIngestionExec);

        }
    }

}
