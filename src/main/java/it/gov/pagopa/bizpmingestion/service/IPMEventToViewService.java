package it.gov.pagopa.bizpmingestion.service;


import it.gov.pagopa.bizpmingestion.entity.cosmos.view.BizEventsViewCart;
import it.gov.pagopa.bizpmingestion.entity.cosmos.view.BizEventsViewGeneral;
import it.gov.pagopa.bizpmingestion.entity.cosmos.view.BizEventsViewUser;
import it.gov.pagopa.bizpmingestion.enumeration.PaymentMethodType;
import it.gov.pagopa.bizpmingestion.exception.AppException;
import it.gov.pagopa.bizpmingestion.model.pm.PMEvent;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventPaymentDetail;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventToViewResult;
import jakarta.validation.constraints.NotNull;

/**
 * Service that map a {@link PMEvent} to its view:
 * <ul>
 * <li>{@link BizEventsViewUser}
 * <li>{@link BizEventsViewGeneral}
 * <li>{@link BizEventsViewCart}
 */
public interface IPMEventToViewService {

    /**
     * Map the provided biz-event to its views
     *
     * @param pmEvent the event to process
     * @return a {@link PMEventToViewResult} that hold the biz-event views
     * @throws AppException
     */
    PMEventToViewResult mapPMEventToView(@NotNull PMEvent pmEvent, @NotNull PMEventPaymentDetail pmEventPaymentDetail) throws AppException;
}
