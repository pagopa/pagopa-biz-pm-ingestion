package it.gov.pagopa.bizpmingestion.model;

import it.gov.pagopa.bizpmingestion.entity.cosmos.execution.SkippedTransaction;
import it.gov.pagopa.bizpmingestion.entity.cosmos.view.BizEventsViewCart;
import it.gov.pagopa.bizpmingestion.entity.cosmos.view.BizEventsViewGeneral;
import it.gov.pagopa.bizpmingestion.entity.cosmos.view.BizEventsViewUser;

import java.util.ArrayList;
import java.util.List;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class WrapperObject {

  private SkippedTransaction skippedTransaction;
  private List<BizEventsViewUser> userViewList = new ArrayList<>();
  private BizEventsViewGeneral generalView;
  private BizEventsViewCart cartView;
}
