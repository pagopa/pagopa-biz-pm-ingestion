package it.gov.pagopa.bizpmingestion.service.impl;

import it.gov.pagopa.bizpmingestion.entity.pm.PPTransaction;
import it.gov.pagopa.bizpmingestion.enumeration.PaymentMethodType;
import it.gov.pagopa.bizpmingestion.model.pm.PMEvent;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventPaymentDetail;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventToViewResult;
import it.gov.pagopa.bizpmingestion.repository.BizEventsViewCartRepository;
import it.gov.pagopa.bizpmingestion.repository.BizEventsViewGeneralRepository;
import it.gov.pagopa.bizpmingestion.repository.BizEventsViewUserRepository;
import it.gov.pagopa.bizpmingestion.service.IPMEventToViewService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;


@Service
@Slf4j
public class TransactionService {

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private IPMEventToViewService pmEventToViewService;
    @Autowired
    private BizEventsViewGeneralRepository bizEventsViewGeneralRepository;
    @Autowired
    private BizEventsViewCartRepository bizEventsViewCartRepository;
    @Autowired
    private BizEventsViewUserRepository bizEventsViewUserRepository;


    @Transactional
    public int elaboration(PMEvent pmEvent, PaymentMethodType paymentMethodType) {
        PMEventPaymentDetail pmEventPaymentDetail = pmEvent.getPaymentDetailList()
                .stream()
                .max(Comparator.comparing(PMEventPaymentDetail::getImporto))
                .orElseThrow();
        PMEventToViewResult result = pmEventToViewService.mapPMEventToView(pmEvent, pmEventPaymentDetail, paymentMethodType);
        if (result != null) {
            bizEventsViewGeneralRepository.save(result.getGeneralView());
            bizEventsViewCartRepository.save(result.getCartView());
            bizEventsViewUserRepository.saveAll(result.getUserViewList());
            return 1;
        }
        return 0;
    }

}
