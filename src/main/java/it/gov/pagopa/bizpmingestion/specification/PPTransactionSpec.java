package it.gov.pagopa.bizpmingestion.specification;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;

import it.gov.pagopa.bizpmingestion.entity.pm.PPTransaction;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PPTransactionSpec implements Specification<PPTransaction> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4326675556613149717L;
	
	private static final String CREATION_DATE = "creationDate";
	private String creationDateFrom;
    private String creationDateTo;
    private List<String> taxCodes;

	public PPTransactionSpec(String dateFrom, String dateTo, List<String> taxCodes) {
		this.creationDateFrom = dateFrom;
		this.creationDateTo = dateTo;
		this.taxCodes = taxCodes;
	}


	public Predicate toPredicate(Root<PPTransaction> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

		query.distinct(true);

		Byte[] statusFilter = {3,8,9,14,21};
		Byte[] accountingStatusFilter = {null,1,5};

		Predicate creationDatePredicate = cb.isTrue(cb.literal(true));
		Predicate predicatePPUserfiscalCode = cb.isTrue(cb.literal(true));

		Join<?, ?> ppUserJoin = root.join("ppUser", JoinType.INNER);
		Join<?, ?> ppWalletJoin = root.join("ppWallet", JoinType.INNER);
		ppWalletJoin.join("ppCreditCard", JoinType.INNER);
		Join<?, ?> ppPaymentJoin = root.join("ppPayment", JoinType.INNER);
		ppPaymentJoin.join("ppPaymentDetail", JoinType.INNER);
		root.join("ppPsp", JoinType.INNER);

		if (!CollectionUtils.isEmpty(taxCodes)) {
			Expression<String> exp = ppUserJoin.get("fiscalCode");
			predicatePPUserfiscalCode = exp.in(taxCodes);
		}
		
		Predicate predicateCreditCard = cb.isNotNull(ppWalletJoin.get("fkCreditCard"));

		Expression<Byte> exp = root.get("status");
		Predicate predicateStatus = exp.in(statusFilter);

		exp = root.get("accountingStatus");
		Predicate predicateAccountingStatus = exp.in(accountingStatusFilter);

		// creation date predicate
		if (creationDateFrom != null && creationDateTo == null) {
			creationDatePredicate = cb.greaterThanOrEqualTo(root.get(CREATION_DATE), 
					Timestamp.valueOf(LocalDate.parse(creationDateFrom, DateTimeFormatter.ISO_DATE).atStartOfDay()));
		} else if (creationDateFrom == null && creationDateTo != null) {
			creationDatePredicate = cb.lessThanOrEqualTo(root.get(CREATION_DATE), 
					Timestamp.valueOf(LocalDate.parse(creationDateTo, DateTimeFormatter.ISO_DATE).atStartOfDay()));
		}
		// The execution proceeds on this branch in only 2 cases: dateFrom and dateTo equal null or both different from null,
		// to check the last case just apply the condition on one of the two dates
		else if (creationDateTo != null) {
			creationDatePredicate = cb.between(root.get(CREATION_DATE), 
					Timestamp.valueOf(LocalDate.parse(creationDateFrom, DateTimeFormatter.ISO_DATE).atStartOfDay()), 
					Timestamp.valueOf(LocalDate.parse(creationDateTo, DateTimeFormatter.ISO_DATE).atStartOfDay()));
		}

		Predicate predicatePPTransactionStatus = cb.and(predicateStatus, predicateAccountingStatus);

		return cb.and(predicatePPUserfiscalCode, cb.and(predicatePPTransactionStatus,predicateCreditCard), creationDatePredicate);
	}
}
