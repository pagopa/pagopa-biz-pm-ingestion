//package it.gov.pagopa.bizpmingestion.mapper;
//
//import it.gov.pagopa.bizpmingestion.entity.pm.*;
//import it.gov.pagopa.bizpmingestion.enumeration.PaymentMethodType;
//import it.gov.pagopa.bizpmingestion.model.pm.PMEvent;
//import it.gov.pagopa.bizpmingestion.model.pm.PMEventPayPal;
//import it.gov.pagopa.bizpmingestion.model.pm.PMEventPaymentDetail;
//import it.gov.pagopa.bizpmingestion.repository.PPPspRepository;
//import org.modelmapper.Converter;
//import org.modelmapper.spi.MappingContext;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.stereotype.Repository;
//
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//
//@Component
//public class ConvertPPTransactionEntityToPMEvent implements Converter<PPTransaction, PMEvent> {
//
//    @Autowired
//    PPPspRepository ppPspRepository;
//
//    @Override
//    public PMEvent convert(MappingContext<PPTransaction, PMEvent> mappingContext) {
//        PPTransaction ppTransaction = mappingContext.getSource();
//
//        String stringCreationDate = ppTransaction.getCreationDate().toString();
//
//        return PMEvent.builder()
//                .pkTransactionId(ppTransaction.getId())
//                .rrn(ppTransaction.getRrn())
//                .numAut(ppTransaction.getNumAut())
//                .creationDate(stringCreationDate)
//                .amount(ppTransaction.getAmount())
//                .fee(ppTransaction.getFee())
//                .grandTotal(ppTransaction.getGrandTotal())
//                .serviceName(ppTransaction.getServiceName())
//                .status(ppTransaction.getStatus())
//                .accountingStatus(ppTransaction.getAccountingStatus())
//                .userFiscalCode(Optional.ofNullable(ppTransaction.getPpUser())
//                        .map(PPUser::getFiscalCode)
//                        .orElse(null))
//                .surname(Optional.ofNullable(ppTransaction.getPpUser())
//                        .map(PPUser::getSurname)
//                        .orElse(null))
//                .name(Optional.ofNullable(ppTransaction.getPpUser())
//                        .map(PPUser::getName)
//                        .orElse(null))
//                .methodType(getMethodType(ppTransaction))
//                .notificationEmail(Optional.ofNullable(ppTransaction.getPpUser())
//                        .map(PPUser::getNotificationEmail)
//                        .orElse(null))
//                .pkPaymentId(Optional.ofNullable(ppTransaction.getPpPayment())
//                        .map(PPPayment::getId)
//                        .orElse(null))
//                .receiver(Optional.ofNullable(ppTransaction.getPpPayment())
//                        .map(PPPayment::getReceiver)
//                        .orElse(null))
//                .subject(Optional.ofNullable(ppTransaction.getPpPayment())
//                        .map(PPPayment::getSubject)
//                        .orElse(null))
//                .idCarrello(Optional.ofNullable(ppTransaction.getPpPayment())
//                        .map(PPPayment::getIdCarrello)
//                        .orElse(null))
//                .origin(Optional.ofNullable(ppTransaction.getPpPayment())
//                        .map(PPPayment::getOrigin)
//                        .orElse(null))
//                .idPayment(Optional.ofNullable(ppTransaction.getPpPayment())
//                        .map(PPPayment::getIdPayment)
//                        .orElse(null))
//                .businessName(Optional.ofNullable(ppTransaction.getPpPsp())
//                        .map(PPPsp::getBusinessName)
//                        .orElse(null))
//                .paymentDetailList(this.getPMPaymentDetailList(Optional.ofNullable(ppTransaction.getPpPayment())
//                        .map(PPPayment::getPpPaymentDetail)
//                        .orElse(Collections.emptyList())))
//                .vposCircuitCode(this.getVposCircuitCode(Optional.ofNullable(ppTransaction.getPpWallet())
//                        .map(PPWallet::getPpCreditCard)
//                        .map(PPCreditCard::getVposCircuitCode)
//                        .orElse("")))
//                .cardNumber(Optional.ofNullable(ppTransaction.getPpWallet())
//                        .map(PPWallet::getPpCreditCard)
//                        .map(PPCreditCard::getCardNumber)
//                        .orElse(""))
//                .payPalList(this.getPMPayPalList(Optional.ofNullable(ppTransaction.getPpWallet())
//                        .map(PPWallet::getPpPayPal)
//                        .orElse(Collections.emptyList())))
//                .build();
//
//    }
//
//    private PaymentMethodType getMethodType(PPTransaction ppTransaction) {
//        if (ppTransaction == null || ppTransaction.getPpWallet() == null || ppTransaction.getPpWallet().getType() == null) {
//            // only precaution. This does not occur
//            return null;
//        }
//        return switch (ppTransaction.getPpWallet().getType().intValue()) {
//            case 1 ->
//                    // credit card
//                    PaymentMethodType.CP;
//            case 5 ->
//                    // paypal
//                    PaymentMethodType.PPAL;
//            case 2, 3 -> {
//                // retrieve from PSP
//                    var psp = ppPspRepository.findById(ppTransaction.getPpWallet().getPpPsp());
//                    if(psp.isPresent()){
//                        yield PaymentMethodType.valueOfFromString(psp.get().getPaymentType());
//                    }
//                    else {
//                        yield PaymentMethodType.UNKNOWN;
//                    }
//            }
//            default -> PaymentMethodType.UNKNOWN;
//        };
//    }
//
//    private String getVposCircuitCode(String vposCircuitCode) {
//        return switch (vposCircuitCode == null ? "" : vposCircuitCode) {
//            case "-2" -> "VPAY";
//            case "-1" -> "OTHER";
//            case "01" -> "VISA";
//            case "02" -> "MASTERCARD";
//            case "04" -> "MAESTRO";
//            case "05" -> "VISA_ELECTRON";
//            case "06" -> "AMEX";
//            case "07" -> "DINERS";
//            default -> "";
//        };
//    }
//
//    private List<PMEventPaymentDetail> getPMPaymentDetailList(List<PPPaymentDetail> ppPaymentDetailList) {
//        List<PMEventPaymentDetail> details = new ArrayList<>();
//        for (PPPaymentDetail ppd : ppPaymentDetailList) {
//            details.add(
//                    PMEventPaymentDetail.builder()
//                            .pkPaymentDetailId(ppd.getId())
//                            .iuv(ppd.getIuv())
//                            .enteBenificiario(ppd.getEnteBeneficiario())
//                            .idDomino(ppd.getIdDominio())
//                            .codicePagatore(ppd.getCodicePagatore())
//                            .nomePagatore(ppd.getNomePagatore())
//                            .importo(ppd.getImporto())
//                            .build()
//            );
//        }
//
//        return details;
//    }
//
//    private List<PMEventPayPal> getPMPayPalList(List<PPPayPal> ppPayPalList) {
//        List<PMEventPayPal> paypalDetails = new ArrayList<>();
//        for (PPPayPal pp : ppPayPalList) {
//            paypalDetails.add(
//                    PMEventPayPal.builder()
//                            .pkPayPalId(pp.getId())
//                            .emailPP(pp.getEmailPP())
//                            .isDefault(pp.getIsDefault())
//                            .build()
//            );
//        }
//        return paypalDetails;
//    }
//}
