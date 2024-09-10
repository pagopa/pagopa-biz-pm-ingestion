package it.gov.pagopa.bizpmingestion.entity.pm;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PP_BPAY")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PPBPay {
    @Id
    @Column(name = "ID_BPAY", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CELLPHONE_NUMBER")
    private String cellphoneNumber;
}
