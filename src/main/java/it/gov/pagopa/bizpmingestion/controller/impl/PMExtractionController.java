package it.gov.pagopa.bizpmingestion.controller.impl;

import it.gov.pagopa.bizpmingestion.controller.IPMExtractionController;
import it.gov.pagopa.bizpmingestion.enumeration.PMExtractionType;
import it.gov.pagopa.bizpmingestion.model.DataExtractionOptionsModel;
import it.gov.pagopa.bizpmingestion.model.ExtractionResponse;
import it.gov.pagopa.bizpmingestion.service.IPMExtractionService;
import it.gov.pagopa.bizpmingestion.util.CommonUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;

@Controller
@Slf4j
public class PMExtractionController implements IPMExtractionController {

    private final IPMExtractionService pmExtractionService;

    @Autowired
    public PMExtractionController(IPMExtractionService pmExtractionService) {
        this.pmExtractionService = pmExtractionService;
    }

    @Override
    public ResponseEntity<ExtractionResponse> pmDataExtraction(PMExtractionType pmExtractionType,
                                                               DataExtractionOptionsModel dataExtractionOptionsModel) {
        var body = pmExtractionService.pmDataExtraction(dataExtractionOptionsModel.getCreationDateFrom(), dataExtractionOptionsModel.getCreationDateTo(), dataExtractionOptionsModel.getTaxCodes(), pmExtractionType);
        return ResponseEntity.ok(body);
    }
}
