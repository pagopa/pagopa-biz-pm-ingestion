package it.gov.pagopa.bizpmingestion.service;

import it.gov.pagopa.bizpmingestion.enumeration.PMExtractionType;

import java.util.List;

import it.gov.pagopa.bizpmingestion.model.ExtractionResponse;
import org.springframework.http.ResponseEntity;


public interface IPMExtractionService {

	ExtractionResponse pmDataExtraction(String dateFrom, String dateTo, List<String> taxCodes, PMExtractionType pmExtractionType);
}
