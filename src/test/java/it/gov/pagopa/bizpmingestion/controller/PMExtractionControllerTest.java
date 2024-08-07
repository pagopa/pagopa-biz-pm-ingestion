package it.gov.pagopa.bizpmingestion.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import it.gov.pagopa.bizpmingestion.entity.pm.PPBPay;
import it.gov.pagopa.bizpmingestion.entity.pm.PPCreditCard;
import it.gov.pagopa.bizpmingestion.entity.pm.PPPayPal;
import it.gov.pagopa.bizpmingestion.entity.pm.PPPayment;
import it.gov.pagopa.bizpmingestion.entity.pm.PPPaymentDetail;
import it.gov.pagopa.bizpmingestion.entity.pm.PPPsp;
import it.gov.pagopa.bizpmingestion.entity.pm.PPTransaction;
import it.gov.pagopa.bizpmingestion.entity.pm.PPUser;
import it.gov.pagopa.bizpmingestion.entity.pm.PPWallet;
import it.gov.pagopa.bizpmingestion.enumeration.PMExtractionType;
import it.gov.pagopa.bizpmingestion.model.DataExtractionOptionsModel;
import it.gov.pagopa.bizpmingestion.repository.BizEventsViewCartRepository;
import it.gov.pagopa.bizpmingestion.repository.BizEventsViewGeneralRepository;
import it.gov.pagopa.bizpmingestion.repository.BizEventsViewUserRepository;
import it.gov.pagopa.bizpmingestion.repository.PPTransactionRepository;
import it.gov.pagopa.bizpmingestion.service.impl.PMExtractionService;
import it.gov.pagopa.bizpmingestion.util.CommonUtility;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration
class PMExtractionControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private PPTransactionRepository ppTransactionRepository;
	@MockBean
	private BizEventsViewGeneralRepository bizEventsViewGeneralRepository;
	@MockBean
	private BizEventsViewCartRepository bizEventsViewCartRepository;
	@MockBean
	private BizEventsViewUserRepository bizEventsViewUserRepository;

	private PMExtractionService pmExtractionService;



	@BeforeEach
	void setUp() throws IOException {
		// precondition
		pmExtractionService = Mockito.spy(new PMExtractionService(ppTransactionRepository, bizEventsViewGeneralRepository, bizEventsViewCartRepository, bizEventsViewUserRepository));
		when(ppTransactionRepository.findAll(any(Specification.class))).thenReturn(this.getPPTransactionListForTest());
	}

	@Test
	void pmDataExtraction_OK() throws Exception {
		DataExtractionOptionsModel body = DataExtractionOptionsModel.builder()
				.creationDateFrom("2024-08-01")
				.creationDateTo("2024-08-31")
				.taxCodes(Arrays.asList("CTLLSS74L16H501A"))
				.build();

		mvc.perform(post("/extraction/data")
				.queryParam("pmExtractionType", PMExtractionType.CARD.toString())
				.content(CommonUtility.toJson(body))
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andReturn();
	} 

	private List<PPTransaction> getPPTransactionListForTest () {

		PPUser user = PPUser.builder()
				.id(11111L)
				.fiscalCode("CTLLSS88L18H501A")
				.name("Alessio")
				.surname("Alessio")
				.notificationEmail("notificationEmail")
				.build();

		PPPaymentDetail ppPaymentDetail = PPPaymentDetail.builder()
				.id(33333L)
				.paymentId("paymentId")
				.iuv("iuv")
				.enteBeneficiario("enteBeneficiario")
				.idDominio("idDominio")
				.codicePagatore("CTLLSS74L16H501A")
				.nomePagatore("nomePagatore")
				.build();

		PPPayment payment = PPPayment.builder()
				.id(22222L)
				.idPayment("idPayment")
				.idCarrello("idCarrello")
				.fiscalCode("CTLLSS88L18H501A")
				.receiver("receiver")
				.origin("origin")
				.subject("/TEST/TXT/Diritti di segreteria")
				.ppPaymentDetail(Arrays.asList(ppPaymentDetail))
				.build();

		PPCreditCard ppCreditCard = PPCreditCard.builder()
				.id(55555L)
				.cardNumber("cardNumber")
				.vposCircuitCode("01")
				.build();

		PPBPay ppBPay = PPBPay.builder()
				.id(66666L)
				.cellphoneNumber("cellphoneNumber")
				.build();

		PPPayPal ppPayPal = PPPayPal.builder()
				.id(77777L)
				.emailPP("emailPP")
				.fkWallet(44444L)
				.build();

		PPWallet wallet = PPWallet.builder()
				.id(44444L)
				.type((byte) 5)
				.fkCreditCard(55555L)
				.fkBPay(66666L)
				.ppCreditCard(ppCreditCard)
				.ppBPay(ppBPay)
				.ppPayPal(Arrays.asList(ppPayPal))
				.build();

		PPPsp ppPsp = PPPsp.builder()
				.id(88888L)
				.idPsp("idPsp")
				.businessName("businessName")
				.build();

		PPTransaction ppTransaction = PPTransaction.builder()
				.id(12345L)
				.rrn("rrn")
				.numAut("numAut")
				.creationDate(Timestamp.from(Instant.now()))
				.amount(1000L)
				.fee(100L)
				.grandTotal(1100L)
				.serviceName("serviceName")
				.status((byte) 9)
				.accountingStatus((byte) 5)
				.fkUser(12345L)
				.fkPayment(12345L)
				.fkWallet(12345L)
				.fkPsp(12345L)
				.ppUser(user)
				.ppPayment(payment)
				.ppWallet(wallet)
				.ppPsp(ppPsp)
				.build();

		return Arrays.asList(ppTransaction);
	}
}
