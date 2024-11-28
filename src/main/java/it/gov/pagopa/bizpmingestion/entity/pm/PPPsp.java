package it.gov.pagopa.bizpmingestion.entity.pm;

import it.gov.pagopa.bizpmingestion.enumeration.PaymentMethodType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PP_PSP")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PPPsp {
    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ID_PSP", nullable = false)
    private String idPsp;

    @Column(name = "BUSINESS_NAME")
    private String businessName;

    @Column(name = "PAYMENT_TYPE")
    private PaymentMethodType paymentType;

}
