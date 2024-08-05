package it.gov.pagopa.bizpmingestion.enumeration;

import java.util.Arrays;

/**
 * Enum for transaction payment methods
 */
public enum PaymentMethodType {

	BBT, BP, AD, CP, PO, OBEP, JIF, MYBK, PPAL, UNKNOWN;

	public static boolean isValidPaymentMethod(String origin) {
		return Arrays.stream(values()).anyMatch(it -> it.name().equalsIgnoreCase(origin));
	}
	
	public static PaymentMethodType getPaymentMethodType(PMExtractionType pmExtractionType) {
		switch (pmExtractionType) {
		case CARD:
			return PaymentMethodType.CP;
		case BPAY:
			return PaymentMethodType.JIF;
		case PAYPAL:
			return PaymentMethodType.PPAL;
		default:
			return PaymentMethodType.UNKNOWN;
		}
    }
}
