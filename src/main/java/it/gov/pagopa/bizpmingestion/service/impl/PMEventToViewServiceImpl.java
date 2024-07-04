package it.gov.pagopa.bizpmingestion.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

import it.gov.pagopa.bizpmingestion.entity.cosmos.view.BizEventsViewCart;
import it.gov.pagopa.bizpmingestion.entity.cosmos.view.BizEventsViewGeneral;
import it.gov.pagopa.bizpmingestion.entity.cosmos.view.BizEventsViewUser;
import it.gov.pagopa.bizpmingestion.entity.cosmos.view.UserDetail;
import it.gov.pagopa.bizpmingestion.entity.cosmos.view.WalletInfo;
import it.gov.pagopa.bizpmingestion.entity.pm.PMEvent;
import it.gov.pagopa.bizpmingestion.enumeration.OriginType;
import it.gov.pagopa.bizpmingestion.enumeration.PaymentMethodType;
import it.gov.pagopa.bizpmingestion.exception.AppError;
import it.gov.pagopa.bizpmingestion.exception.AppException;
import it.gov.pagopa.bizpmingestion.model.cosmos.view.PMEventToViewResult;
import it.gov.pagopa.bizpmingestion.service.PMEventToViewService;
import it.gov.pagopa.bizpmingestion.util.PMEventViewValidator;


/**
 * {@inheritDoc}
 */
public class PMEventToViewServiceImpl implements PMEventToViewService {

    private static final String REF_TYPE_IUV = "IUV";
    private static final String CREATION_DATE_FORMAT = "yyyy/MM/dd HH:mm:ss.SSS";


    /**
     * {@inheritDoc}
     * @throws AppException 
     */
    @Override
    public PMEventToViewResult mapPMEventToView(Logger logger, PMEvent pmEvent) throws AppException {
    	UserDetail debtor = Optional.ofNullable(pmEvent).map(this::getDebtor).orElseThrow(); 
    	UserDetail payer  = Optional.ofNullable(pmEvent).map(this::getDebtor).orElseThrow(); 
    	
    	boolean sameDebtorAndPayer = false;

    	if (debtor != null && payer != null && debtor.getTaxCode() != null && debtor.getTaxCode().equals(payer.getTaxCode())) {
    		sameDebtorAndPayer = true;
    		// only the payer user is created when payer and debtor are the same
    		debtor = null;
    	} 

    	List<BizEventsViewUser> userViewToInsert = new ArrayList<>();
    	
    	UUID value = UUID.randomUUID();
    	
    	if (debtor != null) {
    		BizEventsViewUser debtorUserView = buildUserView(pmEvent, debtor, value, false, true);
    		userViewToInsert.add(debtorUserView);
    	}

    	if (payer != null) {
    		BizEventsViewUser payerUserView = buildUserView(pmEvent, payer, value, true, sameDebtorAndPayer);
    		userViewToInsert.add(payerUserView);
    	}

    	PMEventToViewResult result = PMEventToViewResult.builder()
    			.userViewList(userViewToInsert)
    			// TODO: passare in modo dinamico il PaymentMethodType
    			.generalView(buildGeneralView(pmEvent, payer, value, PaymentMethodType.CP))
    			.cartView(buildCartView(pmEvent, sameDebtorAndPayer ? payer : debtor, value))
    			.build();


    	PMEventViewValidator.validate(logger, result, pmEvent);

    	return result;
    }

    UserDetail getDebtor(PMEvent pmEvent) {
        if (StringUtils.hasLength(pmEvent.getNomePagatore()) && StringUtils.hasLength(pmEvent.getCodicePagatore()) && isValidFiscalCode(pmEvent.getCodicePagatore())) {
            return UserDetail.builder()
                    .name(pmEvent.getNomePagatore())
                    .taxCode(pmEvent.getCodicePagatore())
                    .build();
        }
        throw new AppException(AppError.BAD_REQUEST, 
        		"Missing or invalid debtor info [name="+pmEvent.getNomePagatore()+", taxCode="+pmEvent.getCodicePagatore()+"]");
    }
    
    UserDetail getPayer(PMEvent pmEvent) {
    	UserDetail userDetail = UserDetail.builder().build();
    	
        if (StringUtils.hasLength(pmEvent.getName()) && StringUtils.hasLength(pmEvent.getSurname())) {
        	String fullName = String.format("%s %s", pmEvent.getName(), pmEvent.getSurname());
        	userDetail.setName(fullName);
        }
        
        if (StringUtils.hasLength(pmEvent.getFiscalCode()) && isValidFiscalCode(pmEvent.getFiscalCode())) {
        	userDetail.setTaxCode(pmEvent.getFiscalCode());
        }
        return userDetail;
    }

    UserDetail getPayee(PMEvent pmEvent) {
        if (StringUtils.hasLength(pmEvent.getIdDomino())) {
            return UserDetail.builder()
                    .name(pmEvent.getReceiver())
                    .taxCode(pmEvent.getIdDomino())
                    .build();
        }
        throw new AppException(AppError.BAD_REQUEST, 
        		"Missing or invalid payee info [name="+pmEvent.getReceiver()+", taxCode="+pmEvent.getIdDomino()+"]");
    }


    private BizEventsViewCart buildCartView(PMEvent pmEvent, UserDetail user, UUID value) {
    	
    	DateTimeFormatter f = DateTimeFormatter.ofPattern(CREATION_DATE_FORMAT);
    	LocalDateTime ldt = LocalDateTime.parse(pmEvent.getCreationDate() , f) ;
    	
        return BizEventsViewCart.builder()
        		.id(pmEvent.getId())
                .transactionId("PM-"+value+"-"+ldt.getYear())
                .eventId(pmEvent.getId())
                .subject(pmEvent.getSubject())
                .amount(pmEvent.getGrandTotal())
                .debtor(user)
                .payee(getPayee(pmEvent))
                .refNumberType(REF_TYPE_IUV)
                .refNumberValue(pmEvent.getIuv())
                .build();
    }

    private BizEventsViewGeneral buildGeneralView(PMEvent pmEvent, UserDetail payer, UUID value, PaymentMethodType paymentMethodType) {
    	
    	DateTimeFormatter f = DateTimeFormatter.ofPattern(CREATION_DATE_FORMAT);
    	LocalDateTime ldt = LocalDateTime.parse(pmEvent.getCreationDate() , f) ;
    	
        return BizEventsViewGeneral.builder()
        		.id(pmEvent.getId())
                .transactionId("PM-"+value+"-"+ldt.getYear())
                .authCode(pmEvent.getNumAut())
                .rrn(pmEvent.getRrn())
                .transactionDate(pmEvent.getCreationDate())
                .pspName(pmEvent.getBusinessName())
                .walletInfo(
                        WalletInfo.builder()
                                .brand(pmEvent.getVposCircuitCode())
                                .blurredNumber(pmEvent.getCardNumber())
                                .maskedEmail(pmEvent.getEmailPP())
                                .build())
                .payer(payer)
                .fee(pmEvent.getFee())
                .paymentMethod(paymentMethodType)
                .origin(OriginType.PM)
                // TODO: capire come gestire la casistica del carrello (default = no carrello)
                .totalNotice(1)
                .isCart(false)
                .build();
    }

    private BizEventsViewUser buildUserView(PMEvent pmEvent, UserDetail userDetail, UUID value, boolean isPayer, boolean isDebtor) {
    	
    	DateTimeFormatter f = DateTimeFormatter.ofPattern(CREATION_DATE_FORMAT);
    	LocalDateTime ldt = LocalDateTime.parse(pmEvent.getCreationDate() , f) ;
    	
        return BizEventsViewUser.builder()
        		.id(pmEvent.getId())
                .taxCode(userDetail.getTaxCode())
                .transactionId("PM-"+value+"-"+ldt.getYear()+(isPayer?"-p":"-d"))
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

}
