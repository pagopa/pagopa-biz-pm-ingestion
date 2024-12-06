package it.gov.pagopa.bizpmingestion.entity.pm;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;


@NamedEntityGraph(
        name = "transaction-entity-graph",
        attributeNodes = {
                @NamedAttributeNode("ppUser"),
                @NamedAttributeNode("ppPsp"),
                @NamedAttributeNode(value = "ppWallet", subgraph = "ppWallet-subgraph"),
                @NamedAttributeNode(value = "ppPayment", subgraph = "ppPayment-subgraph")
        },
        subgraphs = {
                @NamedSubgraph(
                        name = "ppPayment-subgraph",
                        attributeNodes = {
                                @NamedAttributeNode("ppPaymentDetail")
                        }
                ),
                @NamedSubgraph(
                        name = "ppWallet-subgraph",
                        attributeNodes = {
                                @NamedAttributeNode("ppCreditCard"),
//                                @NamedAttributeNode("ppPsp"),
//                                @NamedAttributeNode("ppPayPal"),
                        }
                )
        }
)
@Entity
@Table(name = "PP_TRANSACTION")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PPTransaction {
    @Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "RRN")
    private String rrn;
    @Column(name = "NUMAUT")
    private String numAut;
    @Column(name = "CREATION_DATE")
    private Timestamp creationDate;
    @Column(name = "AMOUNT")
    private Long amount;
    @Column(name = "FEE")
    private Long fee;
    @Column(name = "GRAND_TOTAL")
    private Long grandTotal;
    @Column(name = "SERVICE_NAME")
    private String serviceName;
    @Column(name = "STATUS")
    private Byte status;
    @Column(name = "ACCOUNTING_STATUS")
    private Byte accountingStatus;

    @Column(name = "FK_USER", nullable = false)
    private Long fkUser;
    @Column(name = "FK_PAYMENT", nullable = false)
    private Long fkPayment;
    @Column(name = "FK_WALLET", nullable = false)
    private Long fkWallet;
    @Column(name = "FK_PSP", nullable = false)
    private Long fkPsp;


    @ManyToOne(targetEntity = PPUser.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_USER", referencedColumnName = "ID_USER", insertable = false, updatable = false)
    private PPUser ppUser;

    @OneToOne(targetEntity = PPPayment.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_PAYMENT", referencedColumnName = "ID", insertable = false, updatable = false)
    private PPPayment ppPayment;

    @OneToOne(targetEntity = PPWallet.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_WALLET", referencedColumnName = "ID_WALLET", insertable = false, updatable = false)
    private PPWallet ppWallet;

    @OneToOne(targetEntity = PPPsp.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_PSP", referencedColumnName = "ID", insertable = false, updatable = false)
    private PPPsp ppPsp;


}
