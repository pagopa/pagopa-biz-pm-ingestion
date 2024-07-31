package it.gov.pagopa.bizpmingestion.repository;

import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;

import it.gov.pagopa.bizpmingestion.entity.pm.PPUser;

public interface PPUserRepository extends JpaRepository<PPUser, Long> {
	
	List<PPUser> findAll(Specification<PPUser> spec);

}
