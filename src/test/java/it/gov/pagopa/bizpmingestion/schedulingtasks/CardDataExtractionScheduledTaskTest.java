package it.gov.pagopa.bizpmingestion.schedulingtasks;

import it.gov.pagopa.bizpmingestion.config.SchedulingConfig;
import it.gov.pagopa.bizpmingestion.model.pm.PMEvent;
import it.gov.pagopa.bizpmingestion.repository.PPTransactionRepository;
import it.gov.pagopa.bizpmingestion.util.Util;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.IOException;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@SpringJUnitConfig(SchedulingConfig.class)
class CardDataExtractionScheduledTaskTest {

    @MockBean
    private PPTransactionRepository ppTransactionRepository;

    @Mock
    private ModelMapper modelMapper;


    @BeforeEach
    void setUp() throws IOException {
        // precondition
        when(ppTransactionRepository.findAll(any(Specification.class))).thenReturn(Util.getPPTransactionListForTest());
        when(modelMapper.map(any(), any())).thenReturn(PMEvent.builder().paymentDetailList(new ArrayList<>()).build());
    }


    @Test
    void manualCardDataExtraction() throws Exception {
        CardDataExtractionScheduledTask scheduler = spy(new CardDataExtractionScheduledTask(modelMapper, ppTransactionRepository));
        scheduler.dataExtraction();
        verify(ppTransactionRepository, times(1)).findAll(any(Specification.class));
    }

}
