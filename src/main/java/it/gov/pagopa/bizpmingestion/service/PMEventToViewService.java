package it.gov.pagopa.bizpmingestion.service;


import it.gov.pagopa.bizpmingestion.entity.cosmos.view.BizEventsViewCart;
import it.gov.pagopa.bizpmingestion.entity.cosmos.view.BizEventsViewGeneral;
import it.gov.pagopa.bizpmingestion.entity.cosmos.view.BizEventsViewUser;
import it.gov.pagopa.bizpmingestion.entity.pm.PMEvent;
import it.gov.pagopa.bizpmingestion.entity.pm.PMEventPaymentDetail;
import it.gov.pagopa.bizpmingestion.enumeration.PaymentMethodType;
import it.gov.pagopa.bizpmingestion.exception.AppException;
import it.gov.pagopa.bizpmingestion.model.cosmos.view.PMEventToViewResult;

/**
 * Service that map a {@link BizEvent} to its view:
 * <ul>
 * <li>{@link BizEventsViewUser}
 * <li>{@link BizEventsViewGeneral}
 * <li>{@link BizEventsViewCart}
 */
public interface PMEventToViewService {

	/**
	 * Map the provided biz-event to its views
	 *
	 * @param bizEvent the event to process
	 * @return a {@link PMEventToViewResult} that hold the biz-event views
	 * @throws AppException
	 */
	PMEventToViewResult mapPMEventToView(PMEvent pmEvent, PMEventPaymentDetail pmEventPaymentDetail, PaymentMethodType paymentMethodType) throws AppException;
}
