package it.gov.pagopa.bizpmingestion.mapper;

import it.gov.pagopa.bizpmingestion.entity.pm.*;
import it.gov.pagopa.bizpmingestion.enumeration.PaymentMethodType;
import it.gov.pagopa.bizpmingestion.model.pm.PMEvent;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventPayPal;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventPaymentDetail;
import org.modelmapper.Converter;
import org.modelmapper.spi.MappingContext;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
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
                .userFiscalCode(Optional.ofNullable(ppTransaction.getPpUser())
                        .map(PPUser::getFiscalCode)
                        .orElse(null))
                .surname(Optional.ofNullable(ppTransaction.getPpUser())
                        .map(PPUser::getSurname)
                        .orElse(null))
                .name(Optional.ofNullable(ppTransaction.getPpUser())
                        .map(PPUser::getName)
                        .orElse(null))
                .methodType(getMethodType(ppTransaction))
                .notificationEmail(Optional.ofNullable(ppTransaction.getPpUser())
                        .map(PPUser::getNotificationEmail)
                        .orElse(null))
                .pkPaymentId(Optional.ofNullable(ppTransaction.getPpPayment())
                        .map(PPPayment::getId)
                        .orElse(null))
                .receiver(Optional.ofNullable(ppTransaction.getPpPayment())
                        .map(PPPayment::getReceiver)
                        .orElse(null))
                .subject(Optional.ofNullable(ppTransaction.getPpPayment())
                        .map(PPPayment::getSubject)
                        .orElse(null))
                .idCarrello(Optional.ofNullable(ppTransaction.getPpPayment())
                        .map(PPPayment::getIdCarrello)
                        .orElse(null))
                .origin(Optional.ofNullable(ppTransaction.getPpPayment())
                        .map(PPPayment::getOrigin)
                        .orElse(null))
                .idPayment(Optional.ofNullable(ppTransaction.getPpPayment())
                        .map(PPPayment::getIdPayment)
                        .orElse(null))
                .businessName(Optional.ofNullable(ppTransaction.getPpPsp())
                        .map(PPPsp::getBusinessName)
                        .orElse(null))
                .paymentDetailList(this.getPMPaymentDetailList(Optional.ofNullable(ppTransaction.getPpPayment())
                        .map(PPPayment::getPpPaymentDetail)
                        .orElse(Collections.emptyList())))
                .vposCircuitCode(this.getVposCircuitCode(Optional.ofNullable(ppTransaction.getPpWallet())
                        .map(PPWallet::getPpCreditCard)
                        .map(PPCreditCard::getVposCircuitCode)
                        .orElse("")))
                .cardNumber(Optional.ofNullable(ppTransaction.getPpWallet())
                        .map(PPWallet::getPpCreditCard)
                        .map(PPCreditCard::getCardNumber)
                        .orElse(""))
                .payPalList(this.getPMPayPalList(Optional.ofNullable(ppTransaction.getPpWallet())
                        .map(PPWallet::getPpPayPal)
                        .orElse(Collections.emptyList())))
                .build();

    }

    private static PaymentMethodType getMethodType(PPTransaction ppTransaction) {
        if (ppTransaction == null || ppTransaction.getPpWallet() == null || ppTransaction.getPpWallet().getType() == null) {
            // only precaution. This does not occur
            return null;
        }
        if (ppTransaction.getPpWallet().getType().intValue() == 1
                && ppTransaction.getPpWallet().getFkCreditCard() != null) {
            // note: type=1 is sufficient
            return PaymentMethodType.CP;
        }
        if (ppTransaction.getPpWallet().getType().intValue() == 2) {
            // TODO: check this type
            return PaymentMethodType.MYBK;
        }
        if (ppTransaction.getPpWallet().getType().intValue() == 3
                && ppTransaction.getPpWallet().getFkBPay() != null) {
            // type=3 is NOT sufficient.
            // 3 is EXTERNAL_PS.
            // We need to check the FK_BPAY != null
            return PaymentMethodType.JIF;
        }
        if (ppTransaction.getPpWallet().getType().intValue() == 5
                && ppTransaction.getPpWallet().getPpPayPal() != null
                && !ppTransaction.getPpWallet().getPpPayPal().isEmpty()) {
            // I'm not sure if type=3 is sufficient
            return PaymentMethodType.PPAL;
        }
        return PaymentMethodType.UNKNOWN;
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
