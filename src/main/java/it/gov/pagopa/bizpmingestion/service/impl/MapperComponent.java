package it.gov.pagopa.bizpmingestion.service.impl;

import it.gov.pagopa.bizpmingestion.entity.pm.*;
import it.gov.pagopa.bizpmingestion.enumeration.PaymentMethodType;
import it.gov.pagopa.bizpmingestion.model.pm.PMEvent;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventPayPal;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventPaymentDetail;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class MapperComponent {

  public PMEvent convert(PPTransaction ppTransaction) {

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
    String stringCreationDate =
        ppTransaction.getCreationDate().toInstant().atZone(ZoneOffset.UTC).format(formatter);

    PPPsp psp = ppTransaction.getPpPsp();

    PaymentMethodType methodType = getMethodType(ppTransaction);
    var builder =
        PMEvent.builder()
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
            .userFiscalCode(
                Optional.ofNullable(ppTransaction.getPpUser())
                    .map(PPUser::getFiscalCode)
                    .orElse(null))
            .surname(
                Optional.ofNullable(ppTransaction.getPpUser()).map(PPUser::getSurname).orElse(null))
            .name(Optional.ofNullable(ppTransaction.getPpUser()).map(PPUser::getName).orElse(null))
            .methodType(methodType)
            .notificationEmail(
                Optional.ofNullable(ppTransaction.getPpUser())
                    .map(PPUser::getNotificationEmail)
                    .orElse(null))
            .pkPaymentId(
                Optional.ofNullable(ppTransaction.getPpPayment())
                    .map(PPPayment::getId)
                    .orElse(null))
            .receiver(
                Optional.ofNullable(ppTransaction.getPpPayment())
                    .map(PPPayment::getReceiver)
                    .orElse(null))
            .subject(
                Optional.ofNullable(ppTransaction.getPpPayment())
                    .map(PPPayment::getSubject)
                    .orElse(null))
            .idCarrello(
                Optional.ofNullable(ppTransaction.getPpPayment())
                    .map(PPPayment::getIdCarrello)
                    .orElse(null))
            .origin(
                Optional.ofNullable(ppTransaction.getPpPayment())
                    .map(PPPayment::getOrigin)
                    .orElse(null))
            .idPayment(
                Optional.ofNullable(ppTransaction.getPpPayment())
                    .map(PPPayment::getIdPayment)
                    .orElse(null))
            .businessName(Optional.ofNullable(psp).map(PPPsp::getBusinessName).orElse("-"))
            .paymentDetailList(
                this.getPMPaymentDetailList(
                    Optional.ofNullable(ppTransaction.getPpPayment())
                        .map(PPPayment::getPpPaymentDetail)
                        .orElse(Collections.emptyList())))
            .vposCircuitCode(
                this.getVposCircuitCode(
                    Optional.ofNullable(ppTransaction.getPpWallet())
                        .map(PPWallet::getPpCreditCard)
                        .map(PPCreditCard::getVposCircuitCode)
                        .orElse("")))
            .cardNumber(
                Optional.ofNullable(ppTransaction.getPpWallet())
                    .map(PPWallet::getPpCreditCard)
                    .map(PPCreditCard::getCardNumber)
                    .orElse(""));
    if (methodType.equals(PaymentMethodType.PPAL)) {
      builder.payPalList(
          this.getPMPayPalList(
              Optional.ofNullable(ppTransaction.getPpWallet())
                  .map(PPWallet::getPpPayPal)
                  .orElse(Collections.emptyList())));
    }
    return builder.build();
  }

  private PaymentMethodType getMethodType(PPTransaction ppTransaction) {
    if (ppTransaction == null
        || ppTransaction.getPpWallet() == null
        || ppTransaction.getPpWallet().getType() == null) {
      // only precaution. This does not occur
      return null;
    }
    return switch (ppTransaction.getPpWallet().getType().intValue()) {
      case 1 ->
          // credit card
          PaymentMethodType.CP;
      case 5 ->
          // paypal
          PaymentMethodType.PPAL;
      case 2, 3 -> {
        // retrieve from PSP
        //                var psp =
        // ppPspRepository.findById(ppTransaction.getPpWallet().getPpPsp());
        var psp = ppTransaction.getPpWallet().getPpPsp();
        if (psp != null) {
          yield PaymentMethodType.valueOfFromString(psp.getPaymentType());
        } else {
          yield PaymentMethodType.UNKNOWN;
        }
      }
      default -> PaymentMethodType.UNKNOWN;
    };
  }

  private String getVposCircuitCode(String vposCircuitCode) {
    return switch (vposCircuitCode == null ? "" : vposCircuitCode) {
      case "-2" -> "VPAY";
      case "-1" -> "OTHER";
      case "01" -> "VISA";
      case "02" -> "MASTERCARD";
      case "04" -> "MAESTRO";
      case "05" -> "VISA_ELECTRON";
      case "06" -> "AMEX";
      case "07" -> "DINERS";
      default -> "";
    };
  }

  private List<PMEventPaymentDetail> getPMPaymentDetailList(
      List<PPPaymentDetail> ppPaymentDetailList) {
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
              .build());
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
              .build());
    }
    return paypalDetails;
  }
}
