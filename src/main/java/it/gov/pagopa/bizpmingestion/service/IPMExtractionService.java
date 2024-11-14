package it.gov.pagopa.bizpmingestion.service;

import it.gov.pagopa.bizpmingestion.enumeration.PMExtractionType;

import java.util.List;

import org.springframework.http.ResponseEntity;


public interface IPMExtractionService {

	ResponseEntity<Void> pmDataExtraction(String dateFrom, String dateTo, List<String> taxCodes, PMExtractionType pmExtractionType);
}
