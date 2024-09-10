package it.gov.pagopa.bizpmingestion.entity.cosmos.view;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletInfo implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -4005663306506913128L;
    private String accountHolder;
    private String brand;
    private String blurredNumber;
    private String maskedEmail;
}
