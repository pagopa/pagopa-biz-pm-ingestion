package it.gov.pagopa.bizpmingestion.service.impl;

import com.microsoft.azure.functions.annotation.ExponentialBackoffRetry;
import it.gov.pagopa.bizpmingestion.entity.cosmos.execution.BizEventsPMIngestionExecution;
import it.gov.pagopa.bizpmingestion.entity.cosmos.execution.SkippedTransaction;
import it.gov.pagopa.bizpmingestion.entity.pm.*;
import it.gov.pagopa.bizpmingestion.enumeration.PaymentMethodType;
import it.gov.pagopa.bizpmingestion.model.pm.PMEvent;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventPayPal;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventPaymentDetail;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventToViewResult;
import it.gov.pagopa.bizpmingestion.repository.*;
import it.gov.pagopa.bizpmingestion.service.IPMEventToViewService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.spi.MappingContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@EnableAsync
@Service
@Slf4j
public class AsyncService {

    private static final String LOG_BASE_HEADER_INFO = "[ClassMethod: %s] - [MethodParamsToLog: %s]";

    private final BizEventsViewGeneralRepository bizEventsViewGeneralRepository;
    private final BizEventsViewCartRepository bizEventsViewCartRepository;
    private final BizEventsViewUserRepository bizEventsViewUserRepository;
    private final PMIngestionExecutionRepository pmIngestionExecutionRepository;
    private final IPMEventToViewService pmEventToViewService;

    @Autowired
    private PPTransactionRepository ppTransactionRepository;
    @Autowired
    private PPPspRepository ppPspRepository;


    @Autowired
    public AsyncService(BizEventsViewGeneralRepository bizEventsViewGeneralRepository, BizEventsViewCartRepository bizEventsViewCartRepository,
                        BizEventsViewUserRepository bizEventsViewUserRepository, PMIngestionExecutionRepository pmIngestionExecutionRepository,
                        IPMEventToViewService pmEventToViewService) {
        this.bizEventsViewGeneralRepository = bizEventsViewGeneralRepository;
        this.bizEventsViewCartRepository = bizEventsViewCartRepository;
        this.bizEventsViewUserRepository = bizEventsViewUserRepository;
        this.pmIngestionExecutionRepository = pmIngestionExecutionRepository;
        this.pmEventToViewService = pmEventToViewService;
    }

    @Async
    public void processDataAsync(Specification<PPTransaction> spec, BizEventsPMIngestionExecution pmIngestionExec) {

        try {

            List<PPTransaction> ppTrList = ppTransactionRepository.findAll(Specification.where(spec));

            pmIngestionExec.setNumRecordFound(ppTrList.size());

            List<PMEvent> pmEventList;
            pmEventList = ppTrList.stream()
                    .map(this::convert)
                    .toList();

            pmIngestionExec.setStatus("DONE");

            List<SkippedTransaction> skippedId = pmEventList.parallelStream()
                    .map(pmEvent -> {
                        try {
                            PMEventPaymentDetail pmEventPaymentDetail = Optional.ofNullable(pmEvent.getPaymentDetailList())
                                    .orElse(Collections.emptyList())
                                    .stream()
                                    .max(Comparator.comparing(PMEventPaymentDetail::getImporto))
                                    .orElseThrow(() -> new RuntimeException(pmEvent.getUserFiscalCode() +" importo null. transactionId=" + pmEvent.getPkTransactionId()));

                            PMEventToViewResult result = pmEventToViewService.mapPMEventToView(pmEvent, pmEventPaymentDetail);
                            if (result != null) {
                                saveOnCosmos(result);

                            } else {
                                throw new RuntimeException("payer and debtor are null. transactionID=" + pmEvent.getPkTransactionId());
                            }
                            return null;
                        } catch (Exception e) {
                            pmIngestionExec.setStatus("DONE WITH SKIP");

                            return SkippedTransaction.builder()
                                    .transactionId(pmEvent.getPkTransactionId())
                                    .cause(e.getMessage())
                                    .build();
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();

            pmIngestionExec.setNumRecordIngested(pmEventList.size() - skippedId.size());
            pmIngestionExec.setSkippedID(skippedId);

        } catch (Exception e) {
            pmIngestionExec.setStatus("FAILED");

            log.error(String.format(LOG_BASE_HEADER_INFO, "processDataAsync", "[processId=" + pmIngestionExec.getId() + "] - Error during asynchronous processing: " + e.getMessage()));
        } finally {
            pmIngestionExec.setEndTime(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ROOT).format(LocalDateTime.now()));
            pmIngestionExecutionRepository.save(pmIngestionExec);
            log.info("Done!");
        }
    }

    @ExponentialBackoffRetry(maxRetryCount = 3, maximumInterval = "00:00:30", minimumInterval = "00:00:10")
    private void saveOnCosmos(PMEventToViewResult result) {
        bizEventsViewGeneralRepository.save(result.getGeneralView());
        bizEventsViewCartRepository.save(result.getCartView());
        bizEventsViewUserRepository.saveAll(result.getUserViewList());
    }




    public PMEvent convert(PPTransaction ppTransaction) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String stringCreationDate = ppTransaction.getCreationDate().toInstant().atZone(ZoneOffset.UTC).format(formatter);

        var psp = ppPspRepository.findById(ppTransaction.getFkPsp());

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
                .businessName(psp.map(PPPsp::getBusinessName).orElse("-"))
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

    private PaymentMethodType getMethodType(PPTransaction ppTransaction) {
        if (ppTransaction == null || ppTransaction.getPpWallet() == null || ppTransaction.getPpWallet().getType() == null) {
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
                var psp = ppPspRepository.findById(ppTransaction.getPpWallet().getPpPsp());
                if(psp.isPresent()){
                    yield PaymentMethodType.valueOfFromString(psp.get().getPaymentType());
                }
                else {
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
