package it.gov.pagopa.bizpmingestion.entity.cosmos.execution;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class SkippedTransaction {
    private Long transactionId;
    private String cause;
}
