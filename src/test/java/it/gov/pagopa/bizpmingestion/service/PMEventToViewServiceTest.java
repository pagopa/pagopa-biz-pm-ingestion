package it.gov.pagopa.bizpmingestion.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Comparator;

import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import it.gov.pagopa.bizpmingestion.entity.pm.PPTransaction;
import it.gov.pagopa.bizpmingestion.enumeration.PaymentMethodType;
import it.gov.pagopa.bizpmingestion.model.pm.PMEvent;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventPaymentDetail;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventToViewResult;
import it.gov.pagopa.bizpmingestion.util.Util;

@SpringBootTest
class PMEventToViewServiceTest {

	@Autowired
	private IPMEventToViewService pmEventToViewService;
	@Autowired
	private ModelMapper modelMapper;

	@Test
	void mapPMEventToView_not_valid_origin_channel() {
		PPTransaction ppTransaction = Util.getPPTransactionListForTest();
		ppTransaction.getPpPayment().setOrigin("not valid origin value");
		PMEvent pmEvent = modelMapper.map(ppTransaction, PMEvent.class);
		PMEventPaymentDetail pmEventPaymentDetail = pmEvent.getPaymentDetailList()
				.stream()
				.max(Comparator.comparing(PMEventPaymentDetail::getImporto))
				.orElseThrow();
		PMEventToViewResult result = pmEventToViewService.mapPMEventToView(pmEvent, pmEventPaymentDetail, PaymentMethodType.CP);
		assertEquals(false, result.getUserViewList().get(0).isHidden());
		assertEquals(true, result.getUserViewList().get(1).isHidden());
	}
	
	@Test
	void mapPMEventToView_valid_origin_channel() {
		PPTransaction ppTransaction = Util.getPPTransactionListForTest();
		ppTransaction.getPpPayment().setOrigin("CITTADINANZA_DIGITALE");
		PMEvent pmEvent = modelMapper.map(ppTransaction, PMEvent.class);
		PMEventPaymentDetail pmEventPaymentDetail = pmEvent.getPaymentDetailList()
				.stream()
				.max(Comparator.comparing(PMEventPaymentDetail::getImporto))
				.orElseThrow();
		PMEventToViewResult result = pmEventToViewService.mapPMEventToView(pmEvent, pmEventPaymentDetail, PaymentMethodType.CP);
		assertEquals(false, result.getUserViewList().get(0).isHidden());
		assertEquals(false, result.getUserViewList().get(1).isHidden());
	}
}
