package it.gov.pagopa.bizpmingestion.schedulingtasks;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import it.gov.pagopa.bizpmingestion.entity.pm.PPTransaction;
import it.gov.pagopa.bizpmingestion.enumeration.PaymentMethodType;
import it.gov.pagopa.bizpmingestion.model.pm.PMEvent;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventPaymentDetail;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventToViewResult;
import it.gov.pagopa.bizpmingestion.repository.BizEventsViewCartRepository;
import it.gov.pagopa.bizpmingestion.repository.BizEventsViewGeneralRepository;
import it.gov.pagopa.bizpmingestion.repository.BizEventsViewUserRepository;
import it.gov.pagopa.bizpmingestion.repository.PPTransactionRepository;
import it.gov.pagopa.bizpmingestion.service.IPMEventToViewService;
import it.gov.pagopa.bizpmingestion.specification.CardExtractionSpec;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@NoArgsConstructor
@ConditionalOnProperty(name = "cron.job.schedule.card.enabled", matchIfMissing = true)
@Slf4j
public class CardDataExtractionScheduledTasks {
	
	private static final String LOG_BASE_HEADER_INFO = "[OperationType: %s] - [ClassMethod: %s] - [MethodParamsToLog: %s]";
	private static final String CRON_JOB = "CRON JOB - CardDataExtractionScheduledTasks";
    private static final String METHOD = "dataExtraction";
	
	@Autowired
	private ModelMapper modelMapper;
	@Autowired
	private PPTransactionRepository ppTransactionRepository;
	@Autowired
	private BizEventsViewGeneralRepository bizEventsViewGeneralRepository;
	@Autowired
	private BizEventsViewCartRepository bizEventsViewCartRepository;
	@Autowired
	private BizEventsViewUserRepository bizEventsViewUserRepository;
	@Autowired
	private IPMEventToViewService pmEventToViewService;

	@Scheduled(cron = "-")
	//@Scheduled(fixedRate = 60000)
	public void dataExtraction() {
		log.info(String.format(LOG_BASE_HEADER_INFO, CRON_JOB, METHOD, "CARD type data extraction running at " + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now())));
        CardExtractionSpec spec = new CardExtractionSpec();
        List<PPTransaction> ppTrList = ppTransactionRepository.findAll(Specification.where(spec));
        log.debug(String.format(LOG_BASE_HEADER_INFO, CRON_JOB, METHOD, "CARD type data extraction info: Found n. "+ppTrList.size()+" transactions to save on Cosmos DB"));
        for (int i=0; i<1; i++) {
        	PMEvent pmEvent = modelMapper.map(ppTrList.get(i), PMEvent.class);
        	for (PMEventPaymentDetail pmEventPaymentDetail: pmEvent.getPaymentDetailList()) {
        		PMEventToViewResult result = pmEventToViewService.mapPMEventToView(pmEvent, pmEventPaymentDetail, PaymentMethodType.CP);
        		bizEventsViewGeneralRepository.save(result.getGeneralView());
        		bizEventsViewCartRepository.save(result.getCartView());
        		bizEventsViewUserRepository.saveAll(result.getUserViewList());
        	}
        }
        log.info(String.format(LOG_BASE_HEADER_INFO, CRON_JOB, METHOD, "CARD type data extraction finished at " + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now())));
	}
}
