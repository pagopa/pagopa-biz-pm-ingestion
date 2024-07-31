package it.gov.pagopa.bizpmingestion.specification;



import org.springframework.data.jpa.domain.Specification;

import it.gov.pagopa.bizpmingestion.entity.pm.PPUser;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class PPUserSpec implements Specification<PPUser> {


    /**
	 * 
	 */
	private static final long serialVersionUID = 1690227765142074058L;

	public Predicate toPredicate(Root<PPUser> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
    	
    	//Byte[] status = {3,8,9,14,21};
    	
    	List status = Arrays.asList("3","8","9","14","21");

    	Join<?, ?> ppTransactionJoin = root.join("ppTransaction", JoinType.INNER);
    		
    	Predicate fiscalCode = cb.equal(root.get("fiscalCode"), "MTTNDR65A07D969V");
    	
    	Expression<Byte> exp = ppTransactionJoin.get("status");
    	Predicate predicate = exp.in(status);
    	//Predicate statusFilter = cb.in(predicate);
    	/*
    	exp = ppTransactionJoin.get("accountingStatus");
    	predicate = exp.in(status);
    	Predicate accountingStatusFilter = cb.in(predicate);
    	
    	cb.or(statusFilter, accountingStatusFilter);*/
    	
    	return cb.and(fiscalCode, predicate);
    }
}
