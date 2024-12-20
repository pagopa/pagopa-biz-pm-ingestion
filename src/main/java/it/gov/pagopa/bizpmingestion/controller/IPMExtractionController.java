package it.gov.pagopa.bizpmingestion.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import it.gov.pagopa.bizpmingestion.enumeration.PMExtractionType;
import it.gov.pagopa.bizpmingestion.model.DataExtractionOptionsModel;
import it.gov.pagopa.bizpmingestion.model.ExtractionResponse;
import it.gov.pagopa.bizpmingestion.model.ProblemJson;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "PM extraction data REST APIs")
@RequestMapping("/extraction/data")
@Validated
public interface IPMExtractionController {

    @Operation(summary = "Request for data extraction from the PM.", security = {@SecurityRequirement(name = "ApiKey"), @SecurityRequirement(name = "ApiKey")}, operationId = "pmDataExtraction")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request paid.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ExtractionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Malformed request.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "401", description = "Wrong or missing function key.", content = @Content(schema = @Schema())),
            @ApiResponse(responseCode = "404", description = "Not found.", content = @Content(schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "409", description = "Conflict.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "422", description = "Unprocessable entity.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class))),
            @ApiResponse(responseCode = "500", description = "Service unavailable.", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = ProblemJson.class)))})
    @PostMapping(produces = {"application/json"}, consumes = {"application/json"})
    ResponseEntity<ExtractionResponse> pmDataExtraction(
            @Schema(requiredMode = Schema.RequiredMode.REQUIRED, implementation = PMExtractionType.class) @RequestParam(required = true, name = "pmExtractionType", defaultValue = "CARD") @NotNull PMExtractionType pmExtractionType,
            @RequestBody DataExtractionOptionsModel dataExtractionOptionsModel);

}
