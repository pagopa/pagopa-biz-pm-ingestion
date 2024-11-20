package it.gov.pagopa.bizpmingestion.entity;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "dummy")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy.class)
public class TransactionMergeDTO{

    @Id
    private Long transactionId;
    private Byte accountingStatus;
    private Long amount;
    private LocalDateTime creationDate;
    private Long fee;
    private Long fkPayment;
    private Long fkPsp;
    private Long fkUser;
    private Long fkWallet;
    private Long grandTotal;
    private String numaut;
    private Long paymentId;
    private String paymentFiscalCode;
    private String idCarrello;
    private String idPayment;
    private String origin;
    private Long paymentDetailPaymentId;
    private Long paymentDetailId;
    private String codicePagatore;
    private String enteBeneficiario;
    private String idDominio;
    private Long importo;
    private String iuv;
    private String nomePagatore;
    private String receiver;
    private String subject;
    private Long pspId;
    private String businessName;
    private String idPsp;
    private Long idUser;
    private String userFiscalCode;
    private String userName;
    private String notificationEmail;
    private String userSurname;
    private Long idWallet;
    private Long fkBpay;
    private Long fkCreditCard;
    private Long idCreditCard;
    private String cardNumber;
    private String vposCircuitCode;
    private String walletType;
    private String rrn;
    private String serviceName;
    private Byte status;
    private String emailPP;
    private String cellphoneNumber;

}
