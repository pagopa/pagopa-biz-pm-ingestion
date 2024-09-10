package it.gov.pagopa.bizpmingestion.mapper;

import it.gov.pagopa.bizpmingestion.entity.pm.PPCreditCard;
import it.gov.pagopa.bizpmingestion.entity.pm.PPPayPal;
import it.gov.pagopa.bizpmingestion.entity.pm.PPPaymentDetail;
import it.gov.pagopa.bizpmingestion.entity.pm.PPTransaction;
import it.gov.pagopa.bizpmingestion.model.pm.PMEvent;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventPayPal;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventPaymentDetail;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConvertPPTransactionEntityToPMEvent implements Converter<PPTransaction, PMEvent> {

    @Override
    public PMEvent convert(MappingContext<PPTransaction, PMEvent> mappingContext) {
        PPTransaction ppTransaction = mappingContext.getSource();

        String stringCreationDate = ppTransaction.getCreationDate().toLocalDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);


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
                .businessName(ppTransaction.getPpPsp().getBusinessName())
                .paymentDetailList(this.getPMPaymentDetailList(ppTransaction.getPpPayment().getPpPaymentDetail()))
                .vposCircuitCode(this.getVposCircuitCode(Optional.ofNullable(ppTransaction.getPpWallet().getPpCreditCard()).map(PPCreditCard::getVposCircuitCode).orElse("")))
                .cardNumber(Optional.ofNullable(ppTransaction.getPpWallet().getPpCreditCard()).map(PPCreditCard::getCardNumber).orElse(""))
                .payPalList(this.getPMPayPalList(ppTransaction.getPpWallet().getPpPayPal()))
                .build();
    }

    private String getVposCircuitCode(String vposCircuitCode) {
        String typeOfCircuitCode;
        switch (vposCircuitCode == null ? "" : vposCircuitCode) {
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
                typeOfCircuitCode = "";
                break;
        }
        return typeOfCircuitCode;
    }

    private List<PMEventPaymentDetail> getPMPaymentDetailList(List<PPPaymentDetail> ppPaymentDetailList) {
        List<PMEventPaymentDetail> details = new ArrayList<>();
        for (PPPaymentDetail ppd : ppPaymentDetailList) {
            details.add(
                    PMEventPaymentDetail.builder()
                            .pkPaymentDetailId(ppd.getId())
                            .iuv(ppd.getIuv())
                            .enteBenificiario(ppd.getEnteBeneficiario())
                            .idDomino(ppd.getIdDominio())
                            .codicePagatore(ppd.getCodicePagatore())
                            .nomePagatore(ppd.getNomePagatore())
                            .importo(ppd.getImporto())
                            .build()
            );
        }

        return details;
    }

    private List<PMEventPayPal> getPMPayPalList(List<PPPayPal> ppPayPalList) {
        List<PMEventPayPal> paypalDetails = new ArrayList<>();
        for (PPPayPal pp : ppPayPalList) {
            paypalDetails.add(
                    PMEventPayPal.builder()
                            .pkPayPalId(pp.getId())
                            .emailPP(pp.getEmailPP())
                            .isDefault(pp.getIsDefault())
                            .build()
            );
        }
        return paypalDetails;
    }
}
