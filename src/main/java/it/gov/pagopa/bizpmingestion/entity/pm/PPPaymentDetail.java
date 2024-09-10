package it.gov.pagopa.bizpmingestion.entity.pm;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "PP_PAYMENT_DETAIL")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PPPaymentDetail {
    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "PAYMENT_ID", nullable = false)
    private String paymentId;
    @Column(name = "IUV", nullable = false)
    private String iuv;

    @Column(name = "ENTE_BENEFICIARIO")
    private String enteBeneficiario;
    @Column(name = "ID_DOMINIO")
    private String idDominio; // CF EC
    @Column(name = "CODICE_PAGATORE")
    private String codicePagatore;
    @Column(name = "NOME_PAGATORE")
    private String nomePagatore;
    @Column(name = "IMPORTO")
    private Long importo;


    @ManyToOne(targetEntity = PPPayment.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PAYMENT_ID", referencedColumnName = "ID", insertable = false, updatable = false)
    private PPPayment ppPayment;
}
