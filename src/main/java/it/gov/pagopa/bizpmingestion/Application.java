package it.gov.pagopa.bizpmingestion;

import java.util.List;
import java.util.logging.Logger;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.domain.Specification;

import it.gov.pagopa.bizpmingestion.entity.pm.PMEvent;
import it.gov.pagopa.bizpmingestion.entity.pm.PMEventPaymentDetail;
import it.gov.pagopa.bizpmingestion.entity.pm.PPTransaction;
import it.gov.pagopa.bizpmingestion.model.cosmos.view.PMEventToViewResult;
import it.gov.pagopa.bizpmingestion.repository.BizEventsViewCartRepository;
import it.gov.pagopa.bizpmingestion.repository.BizEventsViewGeneralRepository;
import it.gov.pagopa.bizpmingestion.repository.BizEventsViewUserRepository;
import it.gov.pagopa.bizpmingestion.repository.PPTransactionRepository;
import it.gov.pagopa.bizpmingestion.service.PMEventToViewService;
import it.gov.pagopa.bizpmingestion.service.impl.PMEventToViewServiceImpl;
import it.gov.pagopa.bizpmingestion.specification.PPTransactionSpec;
import jakarta.transaction.Transactional;

@SpringBootApplication
public class Application implements CommandLineRunner {
	
	
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


	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	@Override
	@Transactional
    public void run(String... args) throws Exception {
		
		Logger logger = Logger.getLogger("logger");
		
        
        /*
        PPUserSpec spec = new PPUserSpec();
        List<PPUser> ppUserList = ppUserRepository.findAll(Specification.where(spec));
        for (PPUser u: ppUserList) {
        	System.out.println("******** user info:" + u);
        	for (PPTransaction t: u.getPpTransaction()) {
        		System.out.println("******** transaction info:" + t);
        	}
        	
        }*/
        PMEventToViewService pmEventToViewService = new PMEventToViewServiceImpl();
        PPTransactionSpec spec = new PPTransactionSpec();
        List<PPTransaction> ppTrList = ppTransactionRepository.findAll(Specification.where(spec));
        for (int i=0; i<1; i++) {
        	PMEvent pmEvent = modelMapper.map(ppTrList.get(i), PMEvent.class);
        	for (PMEventPaymentDetail pmEventPaymentDetail: pmEvent.getPaymentDetailList()) {
        		PMEventToViewResult result = pmEventToViewService.mapPMEventToView(logger, pmEvent, pmEventPaymentDetail);
        		bizEventsViewGeneralRepository.save(result.getGeneralView());
        		bizEventsViewCartRepository.save(result.getCartView());
        		bizEventsViewUserRepository.saveAll(result.getUserViewList());
        	}
        }
        
        
        
        
    }

}
