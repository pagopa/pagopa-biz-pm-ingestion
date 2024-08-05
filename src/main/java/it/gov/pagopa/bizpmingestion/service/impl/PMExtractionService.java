package it.gov.pagopa.bizpmingestion.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import it.gov.pagopa.bizpmingestion.entity.pm.PMEvent;
import it.gov.pagopa.bizpmingestion.entity.pm.PMEventPaymentDetail;
import it.gov.pagopa.bizpmingestion.entity.pm.PPTransaction;
import it.gov.pagopa.bizpmingestion.enumeration.PMExtractionType;
import it.gov.pagopa.bizpmingestion.enumeration.PaymentMethodType;
import it.gov.pagopa.bizpmingestion.model.cosmos.view.PMEventToViewResult;
import it.gov.pagopa.bizpmingestion.repository.BizEventsViewCartRepository;
import it.gov.pagopa.bizpmingestion.repository.BizEventsViewGeneralRepository;
import it.gov.pagopa.bizpmingestion.repository.BizEventsViewUserRepository;
import it.gov.pagopa.bizpmingestion.repository.PPTransactionRepository;
import it.gov.pagopa.bizpmingestion.service.IPMExtractionService;
import it.gov.pagopa.bizpmingestion.service.PMEventToViewService;
import it.gov.pagopa.bizpmingestion.specification.PPTransactionSpec;
import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class PMExtractionService implements IPMExtractionService{
	
	private static final String LOG_BASE_HEADER_INFO = "[ClassMethod: %s] - [MethodParamsToLog: %s]";
    private static final String METHOD = "pmDataExtraction";
    
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
	private PMEventToViewService pmEventToViewService;

	@Override
    public void pmDataExtraction(String dateFrom, String dateTo, List<String> taxCodes, PMExtractionType pmExtractionType) {
		log.info(String.format(LOG_BASE_HEADER_INFO, METHOD, pmExtractionType + " type data extraction running at " + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now())));
        PPTransactionSpec spec = new PPTransactionSpec(dateFrom, dateTo, taxCodes);
        List<PPTransaction> ppTrList = ppTransactionRepository.findAll(Specification.where(spec));
        log.info(String.format(LOG_BASE_HEADER_INFO, METHOD, pmExtractionType + " type data extraction info: Found n. "+ppTrList.size()+" transactions to save on Cosmos DB."
        		+ "Setted Filters: dateFrom="+dateFrom+", dateFrom="+dateTo+", taxCodes="+taxCodes));
        for (int i=0; i<ppTrList.size(); i++) {
        	PMEvent pmEvent = modelMapper.map(ppTrList.get(i), PMEvent.class);
        	for (PMEventPaymentDetail pmEventPaymentDetail: pmEvent.getPaymentDetailList()) {
        		PMEventToViewResult result = pmEventToViewService.mapPMEventToView(pmEvent, pmEventPaymentDetail, PaymentMethodType.getPaymentMethodType(pmExtractionType));
        		bizEventsViewGeneralRepository.save(result.getGeneralView());
        		bizEventsViewCartRepository.save(result.getCartView());
        		bizEventsViewUserRepository.saveAll(result.getUserViewList());
        	}
        }
        log.info(String.format(LOG_BASE_HEADER_INFO, METHOD, pmExtractionType + " type data extraction finished at " + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now())));    
    }

}
