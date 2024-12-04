package it.gov.pagopa.bizpmingestion.entity.cosmos.execution;

import java.util.ArrayList;
import java.util.List;

import com.azure.spring.data.cosmos.core.mapping.Container;

import it.gov.pagopa.bizpmingestion.enumeration.PMExtractionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity model for pmingestion-step-execution
 */
@Container(containerName = "${azure.cosmos.biz-pmingestion-step-execution}", autoCreateContainer = false, ru = "1000")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class BizEventsPMIngestionExecution {
    private String id;
    private String requestId;
    private Integer sliceNumber;
    private String dateFrom;
    private String dateTo;
    private PMExtractionType extractionType;
    private List<String> taxCodesFilter;
    private List<SkippedTransaction> skippedID = new ArrayList<>();
    private Integer numRecordFound = 0;
    private Integer numRecordIngested = 0;
    private String startTime;
    private String endTime;
    @Builder.Default
    private String status = "UNKNOWN";
}
