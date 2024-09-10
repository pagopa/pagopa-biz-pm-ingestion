package it.gov.pagopa.bizpmingestion.model.pm;

import it.gov.pagopa.bizpmingestion.entity.cosmos.view.BizEventsViewCart;
import it.gov.pagopa.bizpmingestion.entity.cosmos.view.BizEventsViewGeneral;
import it.gov.pagopa.bizpmingestion.entity.cosmos.view.BizEventsViewUser;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Model that hold the result of the BizEvent to view mapping
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class PMEventToViewResult {

    @Valid
    private List<BizEventsViewUser> userViewList;
    @Valid
    private BizEventsViewGeneral generalView;
    @Valid
    private BizEventsViewCart cartView;
}
