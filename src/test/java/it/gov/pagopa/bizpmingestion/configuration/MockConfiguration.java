package it.gov.pagopa.bizpmingestion.configuration;

import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import it.gov.pagopa.bizpmingestion.repository.BizEventsViewCartRepository;
import it.gov.pagopa.bizpmingestion.repository.BizEventsViewGeneralRepository;
import it.gov.pagopa.bizpmingestion.repository.BizEventsViewUserRepository;

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
}