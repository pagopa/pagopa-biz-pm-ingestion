package it.gov.pagopa.bizpmingestion.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

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
import it.gov.pagopa.bizpmingestion.service.IPMExtractionService;
import it.gov.pagopa.bizpmingestion.service.IPMEventToViewService;
import it.gov.pagopa.bizpmingestion.specification.BPayExtractionSpec;
import it.gov.pagopa.bizpmingestion.specification.CardExtractionSpec;
import it.gov.pagopa.bizpmingestion.specification.PayPalExtractionSpec;
import it.gov.pagopa.bizpmingestion.util.CommonUtility;
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
	//@Autowired
	private BizEventsViewGeneralRepository bizEventsViewGeneralRepository;
	//@Autowired
	private BizEventsViewCartRepository bizEventsViewCartRepository;
	//@Autowired
	private BizEventsViewUserRepository bizEventsViewUserRepository;
	@Autowired
	private IPMEventToViewService pmEventToViewService;
	
	public PMExtractionService(PPTransactionRepository ppTransactionRepository,
			BizEventsViewGeneralRepository bizEventsViewGeneralRepository,
			BizEventsViewCartRepository bizEventsViewCartRepository,
			BizEventsViewUserRepository bizEventsViewUserRepository) {
		super();
		this.ppTransactionRepository = ppTransactionRepository;
		this.bizEventsViewGeneralRepository = bizEventsViewGeneralRepository;
		this.bizEventsViewCartRepository = bizEventsViewCartRepository;
		this.bizEventsViewUserRepository = bizEventsViewUserRepository;
	}

	@Override
	public void pmDataExtraction(String dateFrom, String dateTo, List<String> taxCodes, PMExtractionType pmExtractionType) {
		log.info(String.format(LOG_BASE_HEADER_INFO, METHOD, CommonUtility.sanitize(pmExtractionType.toString()) + " type data extraction running at " + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now())));
		
		PaymentMethodType paymentMethodType;
		Specification<PPTransaction> spec = null;

		switch (pmExtractionType) {
		case CARD:
			paymentMethodType = PaymentMethodType.CP;
			spec = new CardExtractionSpec(dateFrom, dateTo, taxCodes);
			break;
		case BPAY:
			paymentMethodType = PaymentMethodType.JIF;
			spec = new BPayExtractionSpec(dateFrom, dateTo, taxCodes);
			break;
		case PAYPAL:
			paymentMethodType = PaymentMethodType.PPAL;
			spec = new PayPalExtractionSpec(dateFrom, dateTo, taxCodes);
			break;
		default:
			 throw new AppException(AppError.BAD_REQUEST, 
		        		"Invalid PM extraction type [pmExtractionType="+pmExtractionType+"]");
		}

		List<PPTransaction> ppTrList = ppTransactionRepository.findAll(Specification.where(spec));
		log.info(String.format(LOG_BASE_HEADER_INFO, METHOD, CommonUtility.sanitize(pmExtractionType.toString()) + " type data extraction info: Found n. "+ppTrList.size()+" transactions to save on Cosmos DB."
				+ "Setted Filters: dateFrom="+dateFrom+", dateFrom="+dateTo+", taxCodes="+taxCodes));
		for (int i=0; i<ppTrList.size(); i++) {
			PMEvent pmEvent = modelMapper.map(ppTrList.get(i), PMEvent.class);
			for (PMEventPaymentDetail pmEventPaymentDetail: pmEvent.getPaymentDetailList()) {
				PMEventToViewResult result = pmEventToViewService.mapPMEventToView(pmEvent, pmEventPaymentDetail, paymentMethodType);
				bizEventsViewGeneralRepository.save(result.getGeneralView());
				bizEventsViewCartRepository.save(result.getCartView());
				bizEventsViewUserRepository.saveAll(result.getUserViewList());
			}
		}
		log.info(String.format(LOG_BASE_HEADER_INFO, METHOD, CommonUtility.sanitize(pmExtractionType.toString()) + " type data extraction finished at " + DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").format(LocalDateTime.now())));    
	}

}
