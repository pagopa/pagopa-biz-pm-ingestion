package it.gov.pagopa.bizpmingestion.repository;

import it.gov.pagopa.bizpmingestion.entity.TransactionMergeDTO;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface TransactionRepository {


    @Query(value = """
            SELECT DISTINCT
             pt.id AS transactionId,
             pt.accounting_status AS accountingStatus,
             pt.amount AS amount,
             pt.creation_date AS creationDate,
             pt.fee AS fee,
             pt.fk_payment AS fkPayment,
             pt.fk_psp AS fkPsp,
             pt.fk_user AS fkUser,
             pt.fk_wallet AS fkWallet,
             pt.grand_total AS grandTotal,
             pt.numaut AS numaut,
             pp.id AS paymentId,
             pp.fiscal_code AS paymentFiscalCode,
             pp.id_carrello AS idCarrello,
             pp.id_payment AS idPayment,
             pp.origin AS origin,
             ppd.payment_id AS paymentDetailPaymentId,
             ppd.id AS paymentDetailId,
             ppd.codice_pagatore AS codicePagatore,
             ppd.ente_beneficiario AS enteBeneficiario,
             ppd.id_dominio AS idDominio,
             ppd.importo AS importo,
             ppd.iuv AS iuv,
             ppd.nome_pagatore AS nomePagatore,
             pp.receiver AS receiver,
             pp.subject AS subject,
             pp2.id AS pspId,
             pp2.business_name AS businessName,
             pp2.id_psp AS idPsp,
             pu.id_user AS idUser,
             pu.fiscal_code AS userFiscalCode,
             pu.name AS userName,
             pu.notification_email AS notificationEmail,
             pu.surname AS userSurname,
             pw.id_wallet AS idWallet,
             pw.fk_bpay AS fkBpay,
             pw.fk_credit_card AS fkCreditCard,
             pcc.id_credit_card AS idCreditCard,
             pcc.card_number AS cardNumber,
             pcc.vpos_circuit_code AS vposCircuitCode,
             pw.type AS walletType,
             pt.rrn AS rrn,
             pt.service_name AS serviceName,
             pt.status AS status
                 FROM AGID_USER.PP_USER pu
                   INNER JOIN AGID_USER.PP_TRANSACTION pt ON pu.ID_USER = pt.FK_USER
                   INNER JOIN AGID_USER.PP_PAYMENT pp ON pt.FK_PAYMENT = pp.ID
                   INNER JOIN AGID_USER.PP_WALLET pw ON pt.FK_WALLET =pw.ID_WALLET
                   INNER JOIN AGID_USER.PP_CREDIT_CARD pcc ON pw.FK_CREDIT_CARD = pcc.ID_CREDIT_CARD
                   INNER JOIN AGID_USER.PP_PSP pp2 ON pt.FK_PSP = pp2.ID
                   INNER JOIN AGID_USER.PP_PAYMENT_DETAIL ppd ON pp.ID = ppd.PAYMENT_ID
                 WHERE (pt.STATUS IN ('3','8','9','14','21') AND pt.ACCOUNTING_STATUS IN (NULL , '1','5')
                   OR (pt.STATUS IN ('3','8','9','14','21') AND pt.ACCOUNTING_STATUS IS null))
                   AND pw.FK_CREDIT_CARD IS NOT null
                   AND pt.creation_date BETWEEN :startDate AND :endDate
            """, nativeQuery = true)
    List<TransactionMergeDTO> findTransactionsByCard(
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate);


    @Query(value = """
                SELECT DISTINCT
                    pt.id AS transactionId,
                    pt.accounting_status AS accountingStatus,
                    pt.amount AS amount,
                    pt.creation_date AS creationDate,
                    pt.fee AS fee,
                    pt.fk_payment AS fkPayment,
                    pt.fk_psp AS fkPsp,
                    pt.fk_user AS fkUser,
                    pt.fk_wallet AS fkWallet,
                    pt.grand_total AS grandTotal,
                    pt.numaut AS numaut,
                    pp.id AS paymentId,
                    pp.fiscal_code AS paymentFiscalCode,
                    pp.id_carrello AS idCarrello,
                    pp.id_payment AS idPayment,
                    pp.origin AS origin,
                    ppd.payment_id AS paymentDetailPaymentId,
                    ppd.id AS paymentDetailId,
                    ppd.codice_pagatore AS codicePagatore,
                    ppd.ente_beneficiario AS enteBeneficiario,
                    ppd.id_dominio AS idDominio,
                    ppd.importo AS importo,
                    ppd.iuv AS iuv,
                    ppd.nome_pagatore AS nomePagatore,
                    pp.receiver AS receiver,
                    pp.subject AS subject,
                    pp2.id AS pspId,
                    pp2.business_name AS businessName,
                    pp2.id_psp AS idPsp,
                    pu.id_user AS idUser,
                    pu.fiscal_code AS userFiscalCode,
                    pu.name AS userName,
                    pu.notification_email AS notificationEmail,
                    pu.surname AS userSurname,
                    pw.id_wallet AS idWallet,
                    pw.fk_bpay AS fkBpay,
                    pw.fk_credit_card AS fkCreditCard,
                    pcc.id_credit_card AS idCreditCard,
                    pcc.card_number AS cardNumber,
                    pcc.vpos_circuit_code AS vposCircuitCode,
                    pw.type AS walletType,
                    pt.rrn AS rrn,
                    pt.service_name AS serviceName,
                    pt.status AS status
                    pp3.EMAIL_PP as emailPP
                    FROM AGID_USER.PP_USER pu
                    INNER JOIN AGID_USER.PP_TRANSACTION pt ON pu.ID_USER = pt.FK_USER
                    INNER JOIN AGID_USER.PP_PAYMENT pp ON pt.FK_PAYMENT = pp.ID
                    INNER JOIN AGID_USER.PP_WALLET pw ON pt.FK_WALLET =pw.ID_WALLET
                    INNER JOIN AGID_USER.PP_PAYPAL pp3  ON pp3.FK_WALLET  = pw.ID_WALLET
                    INNER JOIN AGID_USER.PP_PSP pp2 ON pt.FK_PSP = pp2.ID
                    INNER JOIN AGID_USER.PP_PAYMENT_DETAIL ppd ON pp.ID = ppd.PAYMENT_ID
                WHERE (pt.STATUS IN ('3','8','9','14','21') AND pt.ACCOUNTING_STATUS IN (NULL , '1','5')
                  OR (pt.STATUS IN ('3','8','9','14','21') AND pt.ACCOUNTING_STATUS IS null))
                  AND pw."TYPE" ='5'
                  AND pt.creation_date BETWEEN :startDate AND :endDate
            """, nativeQuery = true)
    List<TransactionMergeDTO> findTransactionsByPayPal(
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate);


    @Query(value = """
                SELECT DISTINCT
                    pt.id AS transactionId,
                    pt.accounting_status AS accountingStatus,
                    pt.amount AS amount,
                    pt.creation_date AS creationDate,
                    pt.fee AS fee,
                    pt.fk_payment AS fkPayment,
                    pt.fk_psp AS fkPsp,
                    pt.fk_user AS fkUser,
                    pt.fk_wallet AS fkWallet,
                    pt.grand_total AS grandTotal,
                    pt.numaut AS numaut,
                    pp.id AS paymentId,
                    pp.fiscal_code AS paymentFiscalCode,
                    pp.id_carrello AS idCarrello,
                    pp.id_payment AS idPayment,
                    pp.origin AS origin,
                    ppd.payment_id AS paymentDetailPaymentId,
                    ppd.id AS paymentDetailId,
                    ppd.codice_pagatore AS codicePagatore,
                    ppd.ente_beneficiario AS enteBeneficiario,
                    ppd.id_dominio AS idDominio,
                    ppd.importo AS importo,
                    ppd.iuv AS iuv,
                    ppd.nome_pagatore AS nomePagatore,
                    pp.receiver AS receiver,
                    pp.subject AS subject,
                    pp2.id AS pspId,
                    pp2.business_name AS businessName,
                    pp2.id_psp AS idPsp,
                    pu.id_user AS idUser,
                    pu.fiscal_code AS userFiscalCode,
                    pu.name AS userName,
                    pu.notification_email AS notificationEmail,
                    pu.surname AS userSurname,
                    pw.id_wallet AS idWallet,
                    pw.fk_bpay AS fkBpay,
                    pw.fk_credit_card AS fkCreditCard,
                    pcc.id_credit_card AS idCreditCard,
                    pcc.card_number AS cardNumber,
                    pcc.vpos_circuit_code AS vposCircuitCode,
                    pw.type AS walletType,
                    pt.rrn AS rrn,
                    pt.service_name AS serviceName,
                    pt.status AS status
                    pp3.EMAIL_PP as emailPP
                    pb.CELLPHONE_NUMBER as cellphoneNumber
                FROM AGID_USER.PP_USER pu
                    INNER JOIN AGID_USER.PP_TRANSACTION pt ON pu.ID_USER = pt.FK_USER
                    INNER JOIN AGID_USER.PP_PAYMENT pp ON pt.FK_PAYMENT = pp.ID
                    INNER JOIN AGID_USER.PP_WALLET pw ON pt.FK_WALLET =pw.ID_WALLET
                    INNER JOIN AGID_USER.PP_BPAY pb  ON pw.FK_BPAY  = pb.ID_BPAY
                    INNER JOIN AGID_USER.PP_PSP pp2 ON pt.FK_PSP = pp2.ID
                    INNER JOIN AGID_USER.PP_PAYMENT_DETAIL ppd ON pp.ID = ppd.PAYMENT_ID
                WHERE (pt.STATUS IN ('3','8','9','14','21') AND pt.ACCOUNTING_STATUS IN (NULL , '1','5')
                  OR (pt.STATUS IN ('3','8','9','14','21') AND pt.ACCOUNTING_STATUS IS null))
                  AND pw.FK_BPAY  IS NOT null
                  AND pt.creation_date BETWEEN :startDate AND :endDate
            """, nativeQuery = true)
    List<TransactionMergeDTO> findTransactionsByBPay(
            @Param("startDate") Timestamp startDate,
            @Param("endDate") Timestamp endDate);
}
