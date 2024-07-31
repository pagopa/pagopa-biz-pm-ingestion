package it.gov.pagopa.bizpmingestion.mapper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;

import it.gov.pagopa.bizpmingestion.entity.pm.PMEvent;
import it.gov.pagopa.bizpmingestion.entity.pm.PMEventPaymentDetail;
import it.gov.pagopa.bizpmingestion.entity.pm.PPPaymentDetail;
import it.gov.pagopa.bizpmingestion.entity.pm.PPTransaction;

public class ConvertPPTransactionEntityToPMEvent implements Converter<PPTransaction, PMEvent> {

    @Override
    public PMEvent convert(MappingContext<PPTransaction, PMEvent> mappingContext) {
        PPTransaction ppTransaction = mappingContext.getSource();
        
        String stringCreationDate = new SimpleDateFormat("yyyy-mm-dd'T'HH:mm:ss").format(ppTransaction.getCreationDate());
        
        return PMEvent.builder()
        		.pkTransactionId(ppTransaction.getId())
        		.rrn(ppTransaction.getRrn())
        		.numAut(ppTransaction.getNumAut())
        		.creationDate(stringCreationDate)
        		.amount(ppTransaction.getAmount())
        		.fee(ppTransaction.getFee())
        		.grandTotal(ppTransaction.getGrandTotal())
        		.serviceName(ppTransaction.getServiceName())
        		.status(ppTransaction.getStatus())
        		.accountingStatus(ppTransaction.getAccountingStatus())
        		.userFiscalCode(ppTransaction.getPpUser().getFiscalCode())
        		.surname(ppTransaction.getPpUser().getSurname())
        		.name(ppTransaction.getPpUser().getName())
        		.notificationEmail(ppTransaction.getPpUser().getNotificationEmail())
        		.pkPaymentId(ppTransaction.getPpPayment().getId())
        		.receiver(ppTransaction.getPpPayment().getReceiver())
        		.subject(ppTransaction.getPpPayment().getSubject())
        		.idCarrello(ppTransaction.getPpPayment().getIdCarrello())
        		.idPayment(ppTransaction.getPpPayment().getIdPayment())
        		.vposCircuitCode(this.getVposCircuitCode(ppTransaction.getPpWallet().getPpCreditCard().getVposCircuitCode()))
        		.cardNumber(ppTransaction.getPpWallet().getPpCreditCard().getCardNumber())
        		.businessName(ppTransaction.getPpPsp().getBusinessName())
        		.paymentDetailList(this.getPMPaymentDetailList(ppTransaction.getPpPayment().getPpPaymentDetail()))
        		.build();
    }
    
	private String getVposCircuitCode(String vposCircuitCode) {
		String typeOfCircuitCode;
		switch (vposCircuitCode==null?"-1":vposCircuitCode) {
		case "-2":
			typeOfCircuitCode = "VPAY";
			break;
		case "-1":
			typeOfCircuitCode = "OTHER";
			break;
		case "01":
			typeOfCircuitCode = "VISA";
			break;
		case "02":
			typeOfCircuitCode = "MASTERCARD";
			break;
		case "04":
			typeOfCircuitCode = "MAESTRO";
			break;
		case "05":
			typeOfCircuitCode = "VISA_ELECTRON";
			break;
		case "06":
			typeOfCircuitCode = "AMEX";
			break;
		case "07":
			typeOfCircuitCode = "DINERS";
			break;
		default:
			//TODO definire cosa fare
			typeOfCircuitCode = "OTHER";
			break;
			//throw new IllegalArgumentException("Invalid vpos circuit code: " + vposCircuitCode);
		}
		return typeOfCircuitCode;
	}
	
	private List<PMEventPaymentDetail> getPMPaymentDetailList (List<PPPaymentDetail> ppPaymentDetailList) {
		List<PMEventPaymentDetail> details = new ArrayList<>();
		for (PPPaymentDetail ppd: ppPaymentDetailList) {
			details.add(
					PMEventPaymentDetail.builder()
					.iuv(ppd.getIuv())
					.enteBenificiario(ppd.getEnteBeneficiario())
					.idDomino(ppd.getIdDominio())
					.codicePagatore(ppd.getCodicePagatore())
					.nomePagatore(ppd.getNomePagatore())
					.build()
					);
		}

		return details;
	}
}
