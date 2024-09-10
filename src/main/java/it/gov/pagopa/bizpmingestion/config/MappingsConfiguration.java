package it.gov.pagopa.bizpmingestion.config;

import it.gov.pagopa.bizpmingestion.entity.pm.PPTransaction;
import it.gov.pagopa.bizpmingestion.mapper.ConvertPPTransactionEntityToPMEvent;
import it.gov.pagopa.bizpmingestion.model.pm.PMEvent;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MappingsConfiguration {

    @Bean
    ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);

        Converter<PPTransaction, PMEvent> convertPPTransactionEntityToPMEvent = new ConvertPPTransactionEntityToPMEvent();
        mapper.createTypeMap(PPTransaction.class, PMEvent.class).setConverter(convertPPTransactionEntityToPMEvent);

        return mapper;
    }

}
