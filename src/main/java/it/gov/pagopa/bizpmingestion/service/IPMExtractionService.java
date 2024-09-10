package it.gov.pagopa.bizpmingestion.service;

import it.gov.pagopa.bizpmingestion.enumeration.PMExtractionType;

import java.util.List;


public interface IPMExtractionService {

    void pmDataExtraction(String dateFrom, String dateTo, List<String> taxCodes, PMExtractionType pmExtractionType);
}
