package it.gov.pagopa.bizpmingestion.mapper;

import it.gov.pagopa.bizpmingestion.entity.TransactionMergeDTO;
import it.gov.pagopa.bizpmingestion.entity.pm.PPPayPal;
import it.gov.pagopa.bizpmingestion.entity.pm.PPPaymentDetail;
import it.gov.pagopa.bizpmingestion.model.pm.PMEvent;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventPayPal;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventPaymentDetail;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ConvertransactionMergeToPMEvent implements Converter<TransactionMergeDTO, PMEvent> {

    @Override
    public PMEvent convert(MappingContext<TransactionMergeDTO, PMEvent> mappingContext) {
        TransactionMergeDTO ppTransaction = mappingContext.getSource();

        String stringCreationDate = ppTransaction.getCreationDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);


        return PMEvent.builder()
                .pkTransactionId(ppTransaction.getTransactionId())
                .rrn(ppTransaction.getRrn())
                .numAut(ppTransaction.getNumaut())
                .creationDate(stringCreationDate)
                .amount(ppTransaction.getAmount())
                .fee(ppTransaction.getFee())
                .grandTotal(ppTransaction.getGrandTotal())
                .serviceName(ppTransaction.getServiceName())
                .status(ppTransaction.getStatus())
                .accountingStatus(ppTransaction.getAccountingStatus())
                .userFiscalCode(ppTransaction.getUserFiscalCode())
                .surname(ppTransaction.getUserSurname())
                .name(ppTransaction.getUserName())
                .notificationEmail(ppTransaction.getNotificationEmail())
                .pkPaymentId(ppTransaction.getPaymentId())
                .receiver(ppTransaction.getReceiver())
                .subject(ppTransaction.getSubject())
                .idCarrello(ppTransaction.getIdCarrello())
                .origin(ppTransaction.getOrigin())
                .idPayment(ppTransaction.getIdPayment())
                .businessName(ppTransaction.getBusinessName())
                .paymentDetailList(getPMPaymentDetailList(ppTransaction))
                .vposCircuitCode(getVposCircuitCode(ppTransaction.getVposCircuitCode()))
                .cardNumber(ppTransaction.getCardNumber())
                .emailPP(ppTransaction.getEmailPP())
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

    private PMEventPaymentDetail getPMPaymentDetailList(TransactionMergeDTO ppPaymentDetailList) {
            return        PMEventPaymentDetail.builder()
                            .pkPaymentDetailId(ppPaymentDetailList.getPaymentDetailId())
                            .iuv(ppPaymentDetailList.getIuv())
                            .enteBenificiario(ppPaymentDetailList.getEnteBeneficiario())
                            .idDomino(ppPaymentDetailList.getIdDominio())
                            .codicePagatore(ppPaymentDetailList.getCodicePagatore())
                            .nomePagatore(ppPaymentDetailList.getNomePagatore())
                            .importo(ppPaymentDetailList.getImporto())
                            .build();

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
