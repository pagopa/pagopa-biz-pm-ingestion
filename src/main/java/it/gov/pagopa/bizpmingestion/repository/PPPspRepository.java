package it.gov.pagopa.bizpmingestion.repository;

import it.gov.pagopa.bizpmingestion.entity.pm.PPPsp;
import it.gov.pagopa.bizpmingestion.entity.pm.PPUser;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PPPspRepository extends JpaRepository<PPPsp, Long> {


}