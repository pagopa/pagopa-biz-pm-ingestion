package it.gov.pagopa.bizpmingestion.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import it.gov.pagopa.bizpmingestion.entity.pm.PMEvent;

public interface PMRepository extends JpaRepository<PMEvent, Integer> {

}
