package it.gov.pagopa.bizpmingestion.entity.cosmos.view;

import com.azure.spring.data.cosmos.core.mapping.Container;

import it.gov.pagopa.bizpmingestion.enumeration.OriginType;
import it.gov.pagopa.bizpmingestion.enumeration.PaymentMethodType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Entity model for biz-events-view-general
 */
@Container(containerName = "${azure.cosmos.biz-events-view-general-container-name}", autoCreateContainer = false, ru="1000")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class BizEventsViewGeneral {
	@NotBlank
	private String id;
	@NotBlank
	private String transactionId;
	private String authCode;
	private PaymentMethodType paymentMethod;
	@NotBlank
	private String rrn;
	@NotBlank
	private String pspName;
	@NotBlank
	private String transactionDate;
	private WalletInfo walletInfo;
	private UserDetail payer;
	private boolean isCart;
	private String fee;
	private OriginType origin;
	private int totalNotice;

}
