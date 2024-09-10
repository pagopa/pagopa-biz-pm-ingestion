package it.gov.pagopa.bizpmingestion.entity.cosmos.view;

import com.azure.spring.data.cosmos.core.mapping.Container;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entity model for biz-events-view-user
 */
@Container(containerName = "${azure.cosmos.biz-events-view-user-container-name}", autoCreateContainer = false, ru = "1000")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class BizEventsViewUser {
    @NotBlank
    private String id;
    @NotBlank
    private String taxCode;
    @NotBlank
    private String transactionId;
    @NotBlank
    private String transactionDate;
    private boolean hidden;
    private Boolean isPayer;
    private Boolean isDebtor;
}
