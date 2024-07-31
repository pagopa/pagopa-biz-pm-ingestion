package it.gov.pagopa.bizpmingestion.entity.pm;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PMEvent {
	private Long pkTransactionId;
	private String userFiscalCode; //pu.FISCAL_CODE
	private String vposCircuitCode;
	private String cardNumber;
	private String surname;
	private String name;
	private String businessName;
	private String rrn;
	private String numAut;
	private String receiver;
	private String subject;
	private String creationDate;
	private Long   amount;
	private Long   fee;
	private Long   grandTotal;
	private String serviceName;
	private String identificationCode;
	private String origin;
	private String debtorFiscalCode; //pp.FISCAL_CODE
	private String idCarrello;
	private Long   pkPaymentId;
	private String idPayment;
	private String notificationEmail;
	private String emailPP;
	private Byte   status;
	private Byte   accountingStatus;
	private List<PMEventPaymentDetail> paymentDetailList;
}
