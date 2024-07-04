package it.gov.pagopa.bizpmingestion; // TODO: refactor the package

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import it.gov.pagopa.bizpmingestion.entity.pm.PPUser;

@SpringBootApplication
public class Application implements CommandLineRunner {
	
	@Autowired
    private JdbcTemplate jdbcTemplate;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
	
	@Override
    public void run(String... args) throws Exception {
        String sql = "SELECT pu.FISCAL_CODE,pu.SURNAME,pu.NAME,pu.NOTIFICATION_EMAIL FROM PP_USER pu WHERE pu.FISCAL_CODE ='MTTNDR65A07D969V'";
         
        List<PPUser> ppUserList = jdbcTemplate.query(sql,
                BeanPropertyRowMapper.newInstance(PPUser.class));
         
        ppUserList.forEach(System.out :: println);
    }

}
