package it.gov.pagopa.bizpmingestion.repository;

import com.azure.spring.data.cosmos.repository.CosmosRepository;

import it.gov.pagopa.bizpmingestion.entity.cosmos.view.BizEventsViewGeneral;

public interface BizEventsViewGeneralRepository extends CosmosRepository<BizEventsViewGeneral, String> {
    
}
