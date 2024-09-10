package it.gov.pagopa.bizpmingestion.schedulingtasks;

import it.gov.pagopa.bizpmingestion.config.SchedulingConfig;
import it.gov.pagopa.bizpmingestion.model.pm.PMEvent;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventPaymentDetail;
import it.gov.pagopa.bizpmingestion.model.pm.PMEventToViewResult;
import it.gov.pagopa.bizpmingestion.repository.BizEventsViewCartRepository;
import it.gov.pagopa.bizpmingestion.repository.BizEventsViewGeneralRepository;
import it.gov.pagopa.bizpmingestion.repository.BizEventsViewUserRepository;
import it.gov.pagopa.bizpmingestion.repository.PPTransactionRepository;
import it.gov.pagopa.bizpmingestion.service.IPMEventToViewService;
import it.gov.pagopa.bizpmingestion.util.Util;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@SpringJUnitConfig(SchedulingConfig.class)
class CardDataExtractionScheduledTaskTest {

    @Mock
    private ModelMapper modelMapper;
    @Mock
    private PPTransactionRepository ppTransactionRepository;
    @Mock
    private BizEventsViewGeneralRepository bizEventsViewGeneralRepository;
    @Mock
    private BizEventsViewCartRepository bizEventsViewCartRepository;
    @Mock
    private BizEventsViewUserRepository bizEventsViewUserRepository;
    @Mock
    private IPMEventToViewService pmEventToViewService;


    @Test
    void manualCardDataExtraction() {
        when(ppTransactionRepository.findAll(any(Specification.class))).thenReturn(Util.getPPTransactionListForTest());
        ArrayList<PMEventPaymentDetail> paymentDetailList = new ArrayList<>();
        paymentDetailList.add(PMEventPaymentDetail.builder().build());
        when(modelMapper.map(any(), any())).thenReturn(PMEvent.builder().paymentDetailList(paymentDetailList).build());
        CardDataExtractionScheduledTask scheduler = spy(new CardDataExtractionScheduledTask(modelMapper, ppTransactionRepository, bizEventsViewGeneralRepository, bizEventsViewCartRepository, bizEventsViewUserRepository, pmEventToViewService));
        when(pmEventToViewService.mapPMEventToView(any(), any(), any())).thenReturn(PMEventToViewResult.builder().build());
        scheduler.dataExtraction();
        verify(ppTransactionRepository, times(1)).findAll(any(Specification.class));
        verify(bizEventsViewGeneralRepository, times(1)).save(any());
        verify(bizEventsViewCartRepository, times(1)).save(any());
        verify(bizEventsViewUserRepository, times(1)).saveAll(any());
    }

    @Test
    void manualCardDataExtraction_DetailNull() {
        when(ppTransactionRepository.findAll(any(Specification.class))).thenReturn(Util.getPPTransactionListForTest());
        when(modelMapper.map(any(), any())).thenReturn(PMEvent.builder().paymentDetailList(new ArrayList<>()).build());
        CardDataExtractionScheduledTask scheduler = spy(new CardDataExtractionScheduledTask(modelMapper, ppTransactionRepository, bizEventsViewGeneralRepository, bizEventsViewCartRepository, bizEventsViewUserRepository, pmEventToViewService));
        scheduler.dataExtraction();
        verify(ppTransactionRepository, times(1)).findAll(any(Specification.class));
        verify(bizEventsViewUserRepository, never()).save(any());
    }

    @Test
    void manualCardDataExtraction_EventNull() {
        ArrayList<Object> list = new ArrayList<>();
        list.add(null);
        when(ppTransactionRepository.findAll(any(Specification.class))).thenReturn(list);
        CardDataExtractionScheduledTask scheduler = spy(new CardDataExtractionScheduledTask(modelMapper, ppTransactionRepository, bizEventsViewGeneralRepository, bizEventsViewCartRepository, bizEventsViewUserRepository, pmEventToViewService));
        scheduler.dataExtraction();
        verify(bizEventsViewUserRepository, never()).save(any());
    }

}
