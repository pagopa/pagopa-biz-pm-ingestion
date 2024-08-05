package it.gov.pagopa.bizpmingestion.service;

import java.util.List;

import org.springframework.stereotype.Service;

import it.gov.pagopa.bizpmingestion.enumeration.PMExtractionType;
import lombok.extern.slf4j.Slf4j;


public interface IPMExtractionService {

    public void pmDataExtraction(String dateFrom, String dateTo, List<String> taxCodes, PMExtractionType pmExtractionType);
}
