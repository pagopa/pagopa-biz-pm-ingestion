package it.gov.pagopa.bizpmingestion.util;

import it.gov.pagopa.bizpmingestion.exception.AppException;
import it.gov.pagopa.bizpmingestion.model.pm.PMEvent;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventToViewResult;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.util.Set;

@Slf4j
public class PMEventViewValidator {

    private PMEventViewValidator() {
    }

    public static void validate(PMEventToViewResult pmEventToViewResult, PMEvent pmEvent) throws AppException {
        Validator validator;
        try (ValidatorFactory factory = jakarta.validation.Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
        Set<ConstraintViolation<PMEventToViewResult>> violations = validator.validate(pmEventToViewResult);

        if (!violations.isEmpty()) {
//            for (ConstraintViolation<PMEventToViewResult> v : violations) {
//                log.error("PMEventToView constraint violation [PMEvent id=" + pmEvent.getPkTransactionId() + ", " + v.getLeafBean().getClass() + ", property:" + v.getPropertyPath() + ", "
//                        + "value:" + v.getInvalidValue() + ", constraints: " + v.getMessageTemplate() + "]");
//            }

            throw new AppException(HttpStatus.BAD_REQUEST, "Validation Error", "Error during PMEventToView validation " + violations + " [BizEvent id=" + pmEvent.getPkTransactionId() + "]");
        }
    }

}
