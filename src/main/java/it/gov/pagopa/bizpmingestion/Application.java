package it.gov.pagopa.bizpmingestion; // TODO: refactor the package

import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import it.gov.pagopa.bizpmingestion.entity.pm.PMEvent;
import it.gov.pagopa.bizpmingestion.entity.pm.PPTransaction;
import it.gov.pagopa.bizpmingestion.entity.pm.PPUser;
import it.gov.pagopa.bizpmingestion.repository.PPTransactionRepository;
import it.gov.pagopa.bizpmingestion.repository.PPUserRepository;
import it.gov.pagopa.bizpmingestion.specification.PPTransactionSpec;
import it.gov.pagopa.bizpmingestion.specification.PPUserSpec;
import jakarta.transaction.Transactional;

@SpringBootApplication
public class Application implements CommandLineRunner {
	
	@Autowired
    private JdbcTemplate jdbcTemplate;
	
	@Autowired
	private ModelMapper modelMapper;
	
	@Autowired
	private PPTransactionRepository ppTransactionRepository;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	@Override
	@Transactional
    public void run(String... args) throws Exception {
        String sql = "SELECT pu.FISCAL_CODE,pu.SURNAME,pu.NAME,pu.NOTIFICATION_EMAIL FROM PP_USER pu WHERE pu.FISCAL_CODE ='MTTNDR65A07D969V'";
         
        /*
        List<PPUser> ppUserList = jdbcTemplate.query(sql,
                BeanPropertyRowMapper.newInstance(PPUser.class));
         
        ppUserList.forEach(System.out :: println);*/
        
        
        /*
        PPUserSpec spec = new PPUserSpec();
        List<PPUser> ppUserList = ppUserRepository.findAll(Specification.where(spec));
        for (PPUser u: ppUserList) {
        	System.out.println("******** user info:" + u);
        	for (PPTransaction t: u.getPpTransaction()) {
        		System.out.println("******** transaction info:" + t);
        	}
        	
        }*/
        
        
        PPTransactionSpec spec = new PPTransactionSpec();
        List<PPTransaction> ppTrList = ppTransactionRepository.findAll(Specification.where(spec));
        int countRows = ppTrList.size();
        for (int i=0; i<ppTrList.size(); i++) {
        	countRows += ppTrList.get(i).getPpPayment().getPpPaymentDetail().size();
        	//System.out.println(i+") ******** transaction info:" + ppTrList.get(i) + ", detail size:" + ppTrList.get(i).getPpPayment().getPpPaymentDetail().size());
        	
        	
        	PMEvent pmEvt = modelMapper.map(ppTrList.get(i), PMEvent.class);
        	System.out.println(i+") ******** pm event info:" + pmEvt );
        }
        System.out.println("------- total rows:" + countRows);
        
    }

}
