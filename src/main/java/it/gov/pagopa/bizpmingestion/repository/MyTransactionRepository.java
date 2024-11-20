package it.gov.pagopa.bizpmingestion.repository;

import it.gov.pagopa.bizpmingestion.entity.TransactionMergeDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public class MyTransactionRepository {

    @Autowired
    private EntityManager entityManager;

    public List<TransactionMergeDTO> findTransactionsByCard(  Timestamp startDate, Timestamp endDate) {
        Query q = entityManager.createNativeQuery(" SELECT DISTINCT\n" +
                "             pt.id AS transactionId,\n" +
                "             pt.accounting_status AS accountingStatus,\n" +
                "             pt.amount AS amount,\n" +
                "             pt.creation_date AS creationDate,\n" +
                "             pt.fee AS fee,\n" +
                "             pt.fk_payment AS fkPayment,\n" +
                "             pt.fk_psp AS fkPsp,\n" +
                "             pt.fk_user AS fkUser,\n" +
                "             pt.fk_wallet AS fkWallet,\n" +
                "             pt.grand_total AS grandTotal,\n" +
                "             pt.numaut AS numaut,\n" +
                "             pp.id AS paymentId,\n" +
                "             pp.fiscal_code AS paymentFiscalCode,\n" +
                "             pp.id_carrello AS idCarrello,\n" +
                "             pp.id_payment AS idPayment,\n" +
                "             pp.origin AS origin,\n" +
                "             ppd.payment_id AS paymentDetailPaymentId,\n" +
                "             ppd.id AS paymentDetailId,\n" +
                "             ppd.codice_pagatore AS codicePagatore,\n" +
                "             ppd.ente_beneficiario AS enteBeneficiario,\n" +
                "             ppd.id_dominio AS idDominio,\n" +
                "             ppd.importo AS importo,\n" +
                "             ppd.iuv AS iuv,\n" +
                "             ppd.nome_pagatore AS nomePagatore,\n" +
                "             pp.receiver AS receiver,\n" +
                "             pp.subject AS subject,\n" +
                "             pp2.id AS pspId,\n" +
                "             pp2.business_name AS businessName,\n" +
                "             pp2.id_psp AS idPsp,\n" +
                "             pu.id_user AS idUser,\n" +
                "             pu.fiscal_code AS userFiscalCode,\n" +
                "             pu.name AS userName,\n" +
                "             pu.notification_email AS notificationEmail,\n" +
                "             pu.surname AS userSurname,\n" +
                "             pw.id_wallet AS idWallet,\n" +
                "             pw.fk_bpay AS fkBpay,\n" +
                "             pw.fk_credit_card AS fkCreditCard,\n" +
                "             pcc.id_credit_card AS idCreditCard,\n" +
                "             pcc.card_number AS cardNumber,\n" +
                "             pcc.vpos_circuit_code AS vposCircuitCode,\n" +
                "             pw.type AS walletType,\n" +
                "             pt.rrn AS rrn,\n" +
                "             pt.service_name AS serviceName,\n" +
                "             pt.status AS status\n" +
                "                 FROM AGID_USER.PP_USER pu\n" +
                "                   INNER JOIN AGID_USER.PP_TRANSACTION pt ON pu.ID_USER = pt.FK_USER\n" +
                "                   INNER JOIN AGID_USER.PP_PAYMENT pp ON pt.FK_PAYMENT = pp.ID\n" +
                "                   INNER JOIN AGID_USER.PP_WALLET pw ON pt.FK_WALLET =pw.ID_WALLET\n" +
                "                   INNER JOIN AGID_USER.PP_CREDIT_CARD pcc ON pw.FK_CREDIT_CARD = pcc.ID_CREDIT_CARD\n" +
                "                   INNER JOIN AGID_USER.PP_PSP pp2 ON pt.FK_PSP = pp2.ID\n" +
                "                   INNER JOIN AGID_USER.PP_PAYMENT_DETAIL ppd ON pp.ID = ppd.PAYMENT_ID\n" +
                "                 WHERE (pt.STATUS IN ('3','8','9','14','21') AND pt.ACCOUNTING_STATUS IN (NULL , '1','5')\n" +
                "                   OR (pt.STATUS IN ('3','8','9','14','21') AND pt.ACCOUNTING_STATUS IS null))\n" +
                "                   AND pw.FK_CREDIT_CARD IS NOT null\n" +
                "                   AND pt.creation_date BETWEEN :startDate AND :endDate");
        q.setParameter("startDate", startDate);
        q.setParameter("endDate", endDate);
        return q.getResultList();
    }
}
