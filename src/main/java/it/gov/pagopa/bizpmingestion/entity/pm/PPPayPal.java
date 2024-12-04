package it.gov.pagopa.bizpmingestion.entity.pm;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "PP_PAYPAL")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PPPayPal {
    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "EMAIL_PP", nullable = false)
    private String emailPP;

    @Column(name = "IS_DEFAULT", nullable = false)
    private Integer isDefault;

    @Column(name = "FK_WALLET", nullable = false)
    private Long fkWallet;

    @ManyToOne(targetEntity = PPWallet.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_WALLET", referencedColumnName = "ID_WALLET", insertable = false, updatable = false)
    private PPWallet ppWallet;
}
