package it.gov.pagopa.bizpmingestion.model.pm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PMEventPayPal {
    private Long pkPayPalId;
    private String emailPP;
}
