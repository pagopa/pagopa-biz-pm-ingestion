package it.gov.pagopa.bizpmingestion.entity.pm;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class PMEvent {
	@Id
	private String id;
	private String fiscalCode;
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
	private String amount;
	private String fee;
	private String grandTotal;
	private String serviceName;
	private String identificationCode;
	private String iuv;
	private String enteBenificiario;
	private String idDomino;
	private String origin;
	private String debtorFiscalCode;
	private String codicePagatore;
	private String nomePagatore;
	private String idCarrello;
	private String idPayment;
	private String notificationEmail;
}
