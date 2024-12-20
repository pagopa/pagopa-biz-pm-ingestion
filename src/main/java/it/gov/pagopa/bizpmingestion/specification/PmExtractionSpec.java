package it.gov.pagopa.bizpmingestion.specification;

import it.gov.pagopa.bizpmingestion.entity.pm.PPTransaction;
import jakarta.persistence.criteria.*;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@NoArgsConstructor
public class PmExtractionSpec implements Specification<PPTransaction> {

    /**
     *
     */
    private static final long serialVersionUID = 4326675556613149717L;

    private static final String CREATION_DATE = "creationDate";
    private static final Byte[] statusFilter = {3, 8, 9, 14, 21};
    private static final Byte[] accountingStatusFilter = {1, 5};
    private String creationDateFrom;
    private String creationDateTo;
    private List<String> taxCodes;

    public PmExtractionSpec(String dateFrom, String dateTo, List<String> taxCodes) {
        this.creationDateFrom = dateFrom;
        this.creationDateTo = dateTo;
        this.taxCodes = taxCodes;
    }


    public Predicate toPredicate(Root<PPTransaction> root, CriteriaQuery<?> query, CriteriaBuilder cb) {

        query.distinct(true);

        Predicate creationDatePredicate = cb.isTrue(cb.literal(true));
        Predicate predicatePPUserfiscalCode = cb.isTrue(cb.literal(true));

        Join<?, ?> ppUserJoin = root.join("ppUser", JoinType.LEFT);
        Join<?, ?> ppPaymentJoin = root.join("ppPayment", JoinType.LEFT);
        Join<?, ?> ppWalletJoin = root.join("ppWallet", JoinType.LEFT);
        root.join("ppPsp", JoinType.LEFT);
        ppWalletJoin.join("ppCreditCard", JoinType.LEFT);
        ppWalletJoin.join("ppBPay", JoinType.LEFT);
        ppWalletJoin.join("ppPsp", JoinType.LEFT);
        ppWalletJoin.join("ppPayPal", JoinType.LEFT);
        Join<?, ?> ppDetailJoin = ppPaymentJoin.join("ppPaymentDetail", JoinType.LEFT);

        Predicate importoMax = cb.max(ppDetailJoin.get("importo")).isNotNull();

        Predicate cardVerification = cb.equal(ppPaymentJoin.get("creditCardVerification"), 0L);

        if (!CollectionUtils.isEmpty(taxCodes)) {
            Expression<String> exp = ppUserJoin.get("fiscalCode");
            predicatePPUserfiscalCode = exp.in(taxCodes);
        }


        Expression<Byte> exp = root.get("status");
        Predicate predicateStatus = exp.in(statusFilter);

        exp = root.get("accountingStatus");
        Predicate predicateAccountingStatus = exp.in(accountingStatusFilter);
        Predicate predicateAccountingStatusIsNull = exp.isNull();

        // creation date predicate
        if (creationDateFrom != null && creationDateTo == null) {
            creationDatePredicate = cb.greaterThanOrEqualTo(root.get(CREATION_DATE),
                    Timestamp.valueOf(LocalDate.parse(creationDateFrom, DateTimeFormatter.ISO_DATE_TIME).atStartOfDay()));
        } else if (creationDateFrom == null && creationDateTo != null) {
            creationDatePredicate = cb.lessThanOrEqualTo(root.get(CREATION_DATE),
                    Timestamp.valueOf(LocalDate.parse(creationDateTo, DateTimeFormatter.ISO_DATE_TIME).atStartOfDay()));
        }
        // The execution proceeds on this branch in only 2 cases: dateFrom and dateTo equal null or both different from null,
        // to check the last case just apply the condition on one of the two dates
        else if (creationDateTo != null) {
            creationDatePredicate = cb.between(root.get(CREATION_DATE),
                    Timestamp.valueOf(LocalDate.parse(creationDateFrom, DateTimeFormatter.ISO_DATE_TIME).atStartOfDay()),
                    Timestamp.valueOf(LocalDate.parse(creationDateTo, DateTimeFormatter.ISO_DATE_TIME).atStartOfDay()));
        }

        Predicate pAccountStatus = cb.or(predicateAccountingStatusIsNull, predicateAccountingStatus);
        Predicate predicatePPTransactionStatus = cb.and(predicateStatus, pAccountStatus);

        return cb.and(predicatePPUserfiscalCode, cb.and(predicatePPTransactionStatus), creationDatePredicate, cardVerification, importoMax);
    }
}
