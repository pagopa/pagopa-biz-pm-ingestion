package it.gov.pagopa.bizpmingestion.controller.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

import it.gov.pagopa.bizpmingestion.controller.IPMExtractionController;
import it.gov.pagopa.bizpmingestion.enumeration.PMExtractionType;
import it.gov.pagopa.bizpmingestion.model.DataExtractionOptionsModel;
import it.gov.pagopa.bizpmingestion.service.IPMExtractionService;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class PMExtractionController implements IPMExtractionController {
	private static final String LOG_BASE_HEADER_INFO = "[RequestMethod: %s] - [ClassMethod: %s] - [MethodParamsToLog: %s]";
	
	@Autowired
	private IPMExtractionService pmExtractionService;
	
	@Override
	public ResponseEntity<Void> pmDataExtraction(PMExtractionType pmExtractionType,
			DataExtractionOptionsModel paymentOptionModel) {
		log.info(String.format(LOG_BASE_HEADER_INFO, "POST", "pmDataExtraction", pmExtractionType +"; " +paymentOptionModel));
		pmExtractionService.pmDataExtraction(paymentOptionModel.getCreationDateFrom(), paymentOptionModel.getCreationDateTo(), paymentOptionModel.getTaxCodes(), pmExtractionType);
		return ResponseEntity.status(HttpStatus.OK).build();
	}
}
