package it.gov.pagopa.bizpmingestion.entity.pm;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PP_CREDIT_CARD")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PPCreditCard {
    @Id
    @Column(name = "ID_CREDIT_CARD", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CARD_NUMBER")
    private String cardNumber;

    @Column(name = "VPOS_CIRCUIT_CODE")
    private String vposCircuitCode;

}
