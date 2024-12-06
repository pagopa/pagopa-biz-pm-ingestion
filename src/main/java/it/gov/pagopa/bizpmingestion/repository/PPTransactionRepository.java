package it.gov.pagopa.bizpmingestion.repository;

import it.gov.pagopa.bizpmingestion.entity.pm.PPTransaction;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PPTransactionRepository extends JpaRepository<PPTransaction, Long> {

  @EntityGraph(value = "transaction-entity-graph")
  List<PPTransaction> findAll(Specification<PPTransaction> spec);

  @EntityGraph(value = "transaction-entity-graph")
  Page<PPTransaction> findAll(Specification<PPTransaction> spec, Pageable pageable);

  long count(Specification<PPTransaction> spec);
}
