package it.gov.pagopa.bizpmingestion.repository;

import com.azure.spring.data.cosmos.repository.CosmosRepository;

import it.gov.pagopa.bizpmingestion.entity.cosmos.view.BizEventsViewCart;

public interface BizEventsViewCartRepository extends CosmosRepository<BizEventsViewCart, String> {
    
}
