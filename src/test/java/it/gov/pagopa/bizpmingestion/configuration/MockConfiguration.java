package it.gov.pagopa.bizpmingestion.configuration;

import it.gov.pagopa.bizpmingestion.repository.BizEventsViewCartRepository;
import it.gov.pagopa.bizpmingestion.repository.BizEventsViewGeneralRepository;
import it.gov.pagopa.bizpmingestion.repository.BizEventsViewUserRepository;
import it.gov.pagopa.bizpmingestion.repository.PMIngestionExecutionRepository;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MockConfiguration {
    @Bean
    @Primary
    BizEventsViewUserRepository bizEventsViewUserRepository() {
        return Mockito.mock(BizEventsViewUserRepository.class);
    }

    @Bean
    @Primary
    BizEventsViewCartRepository bizEventsViewCartRepository() {
        return Mockito.mock(BizEventsViewCartRepository.class);
    }

    @Bean
    @Primary
    BizEventsViewGeneralRepository bizEventsViewGeneralRepository() {
        return Mockito.mock(BizEventsViewGeneralRepository.class);
    }
    
    @Bean
    @Primary
    PMIngestionExecutionRepository pmIngestionExecutionRepository() {
        return Mockito.mock(PMIngestionExecutionRepository.class);
    }
}
