package it.gov.pagopa.bizpmingestion.specification;



import java.util.Arrays;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import it.gov.pagopa.bizpmingestion.entity.pm.PPTransaction;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

public class PPTransactionSpec implements Specification<PPTransaction> {


    /**
	 * 
	 */
	private static final long serialVersionUID = 4326675556613149717L;


	public Predicate toPredicate(Root<PPTransaction> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
		
		query.distinct(true);
    	
    	Byte[] statusFilter = {3,8,9,14,21};
    	Byte[] accountingStatusFilter = {null,1,5};
    	
    	//List<String> statusFilter = Arrays.asList("3","8","9","14","21");
    	//List<String> accountingStatusFilter = Arrays.asList("1","5");

    	Join<?, ?> ppUserJoin = root.join("ppUser", JoinType.INNER);
    	Join<?, ?> ppWalletJoin = root.join("ppWallet", JoinType.INNER);
    	ppWalletJoin.join("ppCreditCard", JoinType.INNER);
    	Join<?, ?> ppPaymentJoin = root.join("ppPayment", JoinType.INNER);
    	ppPaymentJoin.join("ppPaymentDetail", JoinType.INNER);
    	root.join("ppPsp", JoinType.INNER);
    		
    	Predicate predicatePPUserfiscalCode = cb.equal(ppUserJoin.get("fiscalCode"), "MTTNDR65A07D969V");
    	Predicate predicateCreditCard = cb.isNotNull(ppWalletJoin.get("fkCreditCard"));
    	
    	Expression<Byte> exp = root.get("status");
    	Predicate predicateStatus = exp.in(statusFilter);
    	//Predicate statusFilter = cb.in(predicate);
    	
    	exp = root.get("accountingStatus");
    	Predicate predicateAccountingStatus = exp.in(accountingStatusFilter);
    	//Predicate accountingStatusFilter = cb.in(predicate);
    	
    	Predicate predicatePPTransactionStatus = cb.and(predicateStatus, predicateAccountingStatus);
    	
    	return cb.and(predicatePPUserfiscalCode, cb.and(predicatePPTransactionStatus,predicateCreditCard));
    }
}
