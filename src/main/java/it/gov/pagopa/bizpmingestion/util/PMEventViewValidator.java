package it.gov.pagopa.bizpmingestion.util;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.http.HttpStatus;

import it.gov.pagopa.bizpmingestion.entity.pm.PMEvent;
import it.gov.pagopa.bizpmingestion.exception.AppException;
import it.gov.pagopa.bizpmingestion.model.cosmos.view.PMEventToViewResult;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

public class PMEventViewValidator {
	
	private PMEventViewValidator() {}
	
	public static boolean validate(Logger logger, PMEventToViewResult pmEventToViewResult, PMEvent pmEvent) throws AppException {
		ValidatorFactory factory = jakarta.validation.Validation.buildDefaultValidatorFactory();
		Validator validator = factory.getValidator();
		Set<ConstraintViolation<PMEventToViewResult>> violations = validator.validate(pmEventToViewResult);

		if (!violations.isEmpty()) {
			for(ConstraintViolation<PMEventToViewResult> v : violations) {
				logger.log(Level.SEVERE, () -> "PMEventToView constraint violation [PMEvent id="+pmEvent.getPkTransactionId()+", "+v.getLeafBean().getClass()+", property:" + v.getPropertyPath() + ", value:"+  v.getInvalidValue() +", constraints: "+ v.getMessageTemplate() +"]");
			}
			
			throw new AppException(HttpStatus.BAD_REQUEST, "Validation Error", "Error during PMEventToView validation [BizEvent id="+pmEvent.getPkTransactionId()+"]");
		}

		return true;
	}

}
