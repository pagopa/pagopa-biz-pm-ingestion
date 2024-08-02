package it.gov.pagopa.bizpmingestion.repository;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import it.gov.pagopa.bizpmingestion.entity.pm.PPTransaction;

public interface PPTransactionRepository extends JpaRepository<PPTransaction, Long> {
	
	@EntityGraph(value = "transaction-entity-graph")
	List<PPTransaction> findAll(Specification<PPTransaction> spec);

}
