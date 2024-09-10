package it.gov.pagopa.bizpmingestion.util;

import java.util.Set;

import org.springframework.http.HttpStatus;

import it.gov.pagopa.bizpmingestion.exception.AppException;
import it.gov.pagopa.bizpmingestion.model.pm.PMEvent;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventToViewResult;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PMEventViewValidator {
	
	private PMEventViewValidator() {}
	
	public static boolean validate(PMEventToViewResult pmEventToViewResult, PMEvent pmEvent) throws AppException {
		ValidatorFactory factory = jakarta.validation.Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<PMEventToViewResult>> violations = validator.validate(pmEventToViewResult);

		if (!violations.isEmpty()) {
			for(ConstraintViolation<PMEventToViewResult> v : violations) {
				log.error("PMEventToView constraint violation [PMEvent id="+pmEvent.getPkTransactionId()+", "+v.getLeafBean().getClass()+", property:" + v.getPropertyPath() + ", "
						+ "value:"+  v.getInvalidValue() +", constraints: "+ v.getMessageTemplate() +"]");
			}
			
			throw new AppException(HttpStatus.BAD_REQUEST, "Validation Error", "Error during PMEventToView validation [BizEvent id="+pmEvent.getPkTransactionId()+"]");
		}

		return true;
	}

}
