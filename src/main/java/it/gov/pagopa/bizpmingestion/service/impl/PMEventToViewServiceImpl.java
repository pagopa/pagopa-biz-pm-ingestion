package it.gov.pagopa.bizpmingestion.service.impl;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import it.gov.pagopa.bizpmingestion.entity.cosmos.view.BizEventsViewCart;
import it.gov.pagopa.bizpmingestion.entity.cosmos.view.BizEventsViewGeneral;
import it.gov.pagopa.bizpmingestion.entity.cosmos.view.BizEventsViewUser;
import it.gov.pagopa.bizpmingestion.entity.cosmos.view.UserDetail;
import it.gov.pagopa.bizpmingestion.entity.cosmos.view.WalletInfo;
import it.gov.pagopa.bizpmingestion.enumeration.OriginType;
import it.gov.pagopa.bizpmingestion.enumeration.PaymentMethodType;
import it.gov.pagopa.bizpmingestion.exception.AppError;
import it.gov.pagopa.bizpmingestion.exception.AppException;
import it.gov.pagopa.bizpmingestion.model.pm.PMEvent;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventPaymentDetail;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventToViewResult;
import it.gov.pagopa.bizpmingestion.service.IPMEventToViewService;
import it.gov.pagopa.bizpmingestion.util.PMEventViewValidator;


/**
 * {@inheritDoc}
 */
@Service
public class PMEventToViewServiceImpl implements IPMEventToViewService {

    private static final String REF_TYPE_IUV = "IUV";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final String REMITTANCE_INFORMATION_REGEX = "/TXT/(.*)";


    /**
     * {@inheritDoc}
     * @throws AppException 
     */
    @Override
    public PMEventToViewResult mapPMEventToView(PMEvent pmEvent, PMEventPaymentDetail pmEventPaymentDetail, PaymentMethodType paymentMethodType) throws AppException {
    	UserDetail debtor = Optional.ofNullable(pmEventPaymentDetail).map(this::getDebtor).orElseThrow(); 
    	UserDetail payer  = Optional.ofNullable(pmEvent).map(this::getPayer).orElseThrow(); 
    	
    	boolean sameDebtorAndPayer = false;

    	if (debtor != null && payer != null && debtor.getTaxCode() != null && debtor.getTaxCode().equals(payer.getTaxCode())) {
    		sameDebtorAndPayer = true;
    		// only the payer user is created when payer and debtor are the same
    		debtor = null;
    	} 

    	List<BizEventsViewUser> userViewToInsert = new ArrayList<>();
    	
    	if (debtor != null) {
    		BizEventsViewUser debtorUserView = buildUserView(pmEvent, pmEventPaymentDetail, debtor, false, true);
    		userViewToInsert.add(debtorUserView);
    	}

    	if (payer != null) {
    		BizEventsViewUser payerUserView = buildUserView(pmEvent, pmEventPaymentDetail, payer, true, sameDebtorAndPayer);
    		userViewToInsert.add(payerUserView);
    	}

    	PMEventToViewResult result = PMEventToViewResult.builder()
    			.userViewList(userViewToInsert)
    			.generalView(buildGeneralView(pmEvent, pmEventPaymentDetail, payer, paymentMethodType))
    			.cartView(buildCartView(pmEvent, pmEventPaymentDetail, sameDebtorAndPayer ? payer : debtor))
    			.build();


    	PMEventViewValidator.validate(result, pmEvent);

    	return result;
    }

    UserDetail getDebtor(PMEventPaymentDetail pmEventPaymentDetail) {
    	
        if (StringUtils.hasLength(pmEventPaymentDetail.getNomePagatore()) && StringUtils.hasLength(pmEventPaymentDetail.getCodicePagatore()) 
        		&& isValidFiscalCode(pmEventPaymentDetail.getCodicePagatore())) {
            return UserDetail.builder()
                    .name(pmEventPaymentDetail.getNomePagatore())
                    .taxCode(pmEventPaymentDetail.getCodicePagatore())
                    .build();
        }
        throw new AppException(AppError.BAD_REQUEST, 
        		"Missing or invalid debtor info [name="+pmEventPaymentDetail.getNomePagatore()+", taxCode="+pmEventPaymentDetail.getCodicePagatore()+"]");
    }
    
    UserDetail getPayer(PMEvent pmEvent) {
    	UserDetail userDetail = UserDetail.builder().build();
    	
        if (StringUtils.hasLength(pmEvent.getName()) && StringUtils.hasLength(pmEvent.getSurname())) {
        	String fullName = String.format("%s %s", pmEvent.getName(), pmEvent.getSurname());
        	userDetail.setName(fullName);
        }
        
        if (StringUtils.hasLength(pmEvent.getUserFiscalCode()) && isValidFiscalCode(pmEvent.getUserFiscalCode())) {
        	userDetail.setTaxCode(pmEvent.getUserFiscalCode());
        }
        return userDetail;
    }

    UserDetail getPayee(PMEvent pmEvent, PMEventPaymentDetail pmEventPaymentDetail) {
        if (StringUtils.hasLength(pmEventPaymentDetail.getIdDomino())) {
            return UserDetail.builder()
                    .name(pmEvent.getReceiver())
                    .taxCode(pmEventPaymentDetail.getIdDomino())
                    .build();
        }
        throw new AppException(AppError.BAD_REQUEST, 
        		"Missing or invalid payee info [name="+pmEvent.getReceiver()+", taxCode="+pmEventPaymentDetail.getIdDomino()+"]");
    }


    private BizEventsViewCart buildCartView(PMEvent pmEvent, PMEventPaymentDetail pmEventPaymentDetail, UserDetail user) {
    	
    	LocalDateTime ldt = LocalDateTime.parse(pmEvent.getCreationDate() , formatter) ;
    	
        return BizEventsViewCart.builder()
        		.id("PM-"+pmEventPaymentDetail.getPkPaymentDetailId().toString()+"-"+ldt.getYear())
                .transactionId("PM-"+pmEventPaymentDetail.getPkPaymentDetailId().toString()+"-"+ldt.getYear())
                .eventId(pmEvent.getPkTransactionId().toString())
                .subject(this.formatRemittanceInformation(pmEvent.getSubject()))
                .amount(pmEvent.getAmount().toString())
                .debtor(user)
                .payee(getPayee(pmEvent, pmEventPaymentDetail))
                .refNumberType(REF_TYPE_IUV)
                .refNumberValue(pmEventPaymentDetail.getIuv())
                .build();
    }

    private BizEventsViewGeneral buildGeneralView(PMEvent pmEvent, PMEventPaymentDetail pmEventPaymentDetail, UserDetail payer, PaymentMethodType paymentMethodType) {
    	
    	LocalDateTime ldt = LocalDateTime.parse(pmEvent.getCreationDate() , formatter) ;
    	
        return BizEventsViewGeneral.builder()
        		.id("PM-"+pmEventPaymentDetail.getPkPaymentDetailId().toString()+"-"+ldt.getYear())
                .transactionId("PM-"+pmEventPaymentDetail.getPkPaymentDetailId().toString()+"-"+ldt.getYear())
                .authCode(pmEvent.getNumAut())
                .rrn(pmEvent.getRrn())
                .transactionDate(pmEvent.getCreationDate())
                .pspName(pmEvent.getBusinessName())
                .walletInfo(
                        WalletInfo.builder()
                                .brand(pmEvent.getVposCircuitCode())
                                .blurredNumber(pmEvent.getCardNumber())
                                 //TODO chiedere conferma sul comporamento per valorizzare questo campo
                                .maskedEmail(CollectionUtils.isEmpty(pmEvent.getPayPalList()) ? "" : pmEvent.getPayPalList().get(0).getEmailPP()) 
                                .build())
                .payer(payer)
                .fee(currencyFormat(String.valueOf(pmEvent.getFee()/100.00)))
                .paymentMethod(paymentMethodType)
                .origin(OriginType.PM)
                // i pagamenti provenienti dal PM vengono trattati come non di tipo carrello
                .totalNotice(1)
                .isCart(false)
                .build();
    }

    private BizEventsViewUser buildUserView(PMEvent pmEvent, PMEventPaymentDetail pmEventPaymentDetail, UserDetail userDetail, boolean isPayer, boolean isDebtor) {
    	
    	LocalDateTime ldt = LocalDateTime.parse(pmEvent.getCreationDate() , formatter) ;
    	
        return BizEventsViewUser.builder()
        		.id("PM-"+pmEventPaymentDetail.getPkPaymentDetailId().toString()+"-"+ldt.getYear()+(isPayer?"-p":"-d"))
                .transactionId("PM-"+pmEventPaymentDetail.getPkPaymentDetailId().toString()+"-"+ldt.getYear())
                .taxCode(userDetail.getTaxCode())
                .transactionDate(pmEvent.getCreationDate())
                .hidden(false)
                .isPayer(isPayer)
                .isDebtor(isDebtor)
                .build();
    }

    private boolean isValidFiscalCode(String taxCode) {
        if (taxCode != null && !taxCode.isEmpty()) {
            Pattern patternCF = Pattern.compile("^[A-Z]{6}[0-9LMNPQRSTUV]{2}[ABCDEHLMPRST][0-9LMNPQRSTUV]{2}[A-Z][0-9LMNPQRSTUV]{3}[A-Z]$");
            Pattern patternPIVA = Pattern.compile("^\\d{11}");

            return patternCF.matcher(taxCode).find() || patternPIVA.matcher(taxCode).find();
        }
        return false;
    }
    
    private String formatRemittanceInformation(String remittanceInformation) {
        if (remittanceInformation != null) {
            Pattern pattern = Pattern.compile(REMITTANCE_INFORMATION_REGEX);
            // replaceAll with '\R' to remove any unicode linebreak sequence
            Matcher matcher = pattern.matcher(remittanceInformation.replaceAll("\\R", ""));
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        return remittanceInformation;
    }
    
    private String currencyFormat(String value) {
        BigDecimal valueToFormat = new BigDecimal(value);
        NumberFormat numberFormat = NumberFormat.getInstance(Locale.ITALY);
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setMinimumFractionDigits(2);
        return numberFormat.format(valueToFormat);
    }

}
