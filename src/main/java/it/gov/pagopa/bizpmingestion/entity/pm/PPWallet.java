package it.gov.pagopa.bizpmingestion.entity.pm;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "PP_WALLET")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PPWallet {
    @Id
    @Column(name = "ID_WALLET", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "TYPE")
    private Byte type;

    @Column(name = "FK_CREDIT_CARD")
    private Long fkCreditCard;

    @Column(name = "FK_BPAY")
    private Long fkBPay;

    @ManyToOne(targetEntity = PPCreditCard.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_CREDIT_CARD", referencedColumnName = "ID_CREDIT_CARD", insertable = false, updatable = false)
    private PPCreditCard ppCreditCard;

    @ManyToOne(targetEntity = PPBPay.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_BPAY", referencedColumnName = "ID_BPAY", insertable = false, updatable = false)
    private PPBPay ppBPay;

    @Builder.Default
    @OneToMany(targetEntity = PPPayPal.class, fetch = FetchType.EAGER, mappedBy = "ppWallet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PPPayPal> ppPayPal = new ArrayList<>();

    @OneToOne(targetEntity = PPPsp.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "FK_PSP", referencedColumnName = "ID", insertable = false, updatable = false)
    private PPPsp ppPsp;

}
