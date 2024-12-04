package it.gov.pagopa.bizpmingestion.specification;

import it.gov.pagopa.bizpmingestion.entity.pm.PPPayment;
import it.gov.pagopa.bizpmingestion.entity.pm.PPPaymentDetail;
import it.gov.pagopa.bizpmingestion.entity.pm.PPTransaction;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.domain.Specification;

public class TransactionSpecifications {

  public Specification<PPTransaction> getFilteredTransactions(
      LocalDateTime startDate, LocalDateTime endDate) {
    return Specification.where(TransactionSpecifications.distinct())
        .and(TransactionSpecifications.byStatus(List.of("3", "8", "9", "14", "21")))
        .and(TransactionSpecifications.byAccountingStatus(List.of("1", "5")))
        .and(TransactionSpecifications.byCreationDateBetween(startDate, endDate))
        .and(TransactionSpecifications.withMaxDetail())
        .and(TransactionSpecifications.byCreditCardVerification(false));
  }

  public static Specification<PPTransaction> distinct() {
    return (root, query, criteriaBuilder) -> {
      query.distinct(true); // Imposta la query come distinta

      Join<?, ?> ppUserJoin = root.join("ppUser", JoinType.INNER);

      Join<?, ?> ppPaymentJoin = root.join("ppPayment", JoinType.INNER);

      Join<?, ?> ppWalletJoin = root.join("ppWallet", JoinType.LEFT);

      Join<?, ?> ppPspJoin = root.join("ppPsp", JoinType.LEFT);

      Join<?, ?> ppCreditCardJoin = ppWalletJoin.join("ppCreditCard", JoinType.LEFT);
      Join<?, ?> ppPayPalJoin = ppWalletJoin.join("ppPayPal", JoinType.LEFT);
      Join<?, ?> ppPspJoin2 = ppWalletJoin.join("ppPsp", JoinType.LEFT);

      Join<?, ?> ppPaymentDetailJoin = ppPaymentJoin.join("ppPaymentDetail", JoinType.LEFT);

      return criteriaBuilder
          .conjunction(); // Restituisce una condizione vera, per non influire sulla query
    };
  }

  public static Specification<PPTransaction> byStatus(List<String> statuses) {
    return (root, query, criteriaBuilder) -> root.get("status").in(statuses);
  }

  public static Specification<PPTransaction> byAccountingStatus(List<String> statuses) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.or(
            criteriaBuilder.isNull(root.get("accountingStatus")),
            root.get("accountingStatus").in(statuses));
  }

  public static Specification<PPTransaction> byCreationDateBetween(
      LocalDateTime startDate, LocalDateTime endDate) {
    return (root, query, criteriaBuilder) ->
        criteriaBuilder.and(
            criteriaBuilder.greaterThanOrEqualTo(root.get("creationDate"), startDate),
            criteriaBuilder.lessThan(root.get("creationDate"), endDate));
  }

  public static Specification<PPTransaction> withMaxDetail() {
    return (root, query, criteriaBuilder) -> {
      Join<PPTransaction, PPPayment> joinPPP = root.join("ppPayment", JoinType.LEFT);

      // Creazione della sottoquery per MaxDetail
      Subquery<Long> subquery = query.subquery(Long.class);
      Root<PPPaymentDetail> subRoot = subquery.from(PPPaymentDetail.class);
      subquery
          .select(criteriaBuilder.min(subRoot.get("id")))
          .where(criteriaBuilder.equal(subRoot.get("paymentId"), joinPPP.get("id")));

      Join<?, ?> ppPaymentDetailJoin = joinPPP.join("ppPaymentDetail", JoinType.LEFT);
      // Condizione principale
      return criteriaBuilder.equal(ppPaymentDetailJoin.get("id"), subquery);
    };
  }

  public static Specification<PPTransaction> byCreditCardVerification(boolean isVerified) {
    return (root, query, criteriaBuilder) -> {
      Join<PPTransaction, PPPayment> joinPPP = root.join("ppPayment", JoinType.LEFT);
      return criteriaBuilder.equal(joinPPP.get("creditCardVerification"), isVerified ? 1L : 0L);
    };
  }
}
