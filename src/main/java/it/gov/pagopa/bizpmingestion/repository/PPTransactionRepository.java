package it.gov.pagopa.bizpmingestion.repository;

import it.gov.pagopa.bizpmingestion.entity.pm.PPTransaction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PPTransactionRepository extends JpaRepository<PPTransaction, Long> {

    @EntityGraph(value = "transaction-entity-graph")
    List<PPTransaction> findAll(Specification<PPTransaction> spec);

}
