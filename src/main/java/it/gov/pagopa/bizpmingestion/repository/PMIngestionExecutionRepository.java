package it.gov.pagopa.bizpmingestion.repository;

import com.azure.spring.data.cosmos.repository.CosmosRepository;

import it.gov.pagopa.bizpmingestion.entity.cosmos.execution.BizEventsPMIngestionExecution;

public interface PMIngestionExecutionRepository extends CosmosRepository<BizEventsPMIngestionExecution, String> {

}
