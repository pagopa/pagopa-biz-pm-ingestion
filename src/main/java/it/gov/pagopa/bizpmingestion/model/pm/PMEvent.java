package it.gov.pagopa.bizpmingestion.model.pm;

import it.gov.pagopa.bizpmingestion.enumeration.PaymentMethodType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PMEvent {
    private Long pkTransactionId;
    private String userFiscalCode; //pu.FISCAL_CODE
    private String surname;
    private String name;
    private PaymentMethodType methodType;
    private String businessName;
    private String rrn;
    private String numAut;
    private String receiver;
    private String subject;
    private String creationDate;
    private Long amount;
    private Long fee;
    private Long grandTotal;
    private String serviceName;
    private String identificationCode;
    private String origin;
    private String debtorFiscalCode; //pp.FISCAL_CODE
    private String idCarrello;
    private Long pkPaymentId;
    private String idPayment;
    private String notificationEmail;
    private Byte status;
    private Byte accountingStatus;
    private String cardNumber; // only for CARD extraction
    private String vposCircuitCode; // only for CARD extraction
    private String cellphoneNumber; // only for BPAY extraction
    private List<PMEventPayPal> payPalList; // only for PAYPAL extraction
    private List<PMEventPaymentDetail> paymentDetailList;
}
