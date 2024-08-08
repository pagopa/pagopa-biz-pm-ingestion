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
import it.gov.pagopa.bizpmingestion.repository.PPTransactionRepository;
import it.gov.pagopa.bizpmingestion.util.CommonUtility;
import it.gov.pagopa.bizpmingestion.util.Util;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration
class PMExtractionControllerTest {

	@Autowired
	private MockMvc mvc;

	@MockBean
	private PPTransactionRepository ppTransactionRepository;
	

	@BeforeEach
	void setUp() throws IOException {
		// precondition
		when(ppTransactionRepository.findAll(any(Specification.class))).thenReturn(Util.getPPTransactionListForTest());
	}

	@Test
	void pmDataExtraction_CARD_OK() throws Exception {
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
	
	@Test
	void pmDataExtraction_BPAY_OK() throws Exception {
		DataExtractionOptionsModel body = DataExtractionOptionsModel.builder()
				.creationDateFrom("2024-08-01")
				.creationDateTo("2024-08-31")
				.taxCodes(Arrays.asList("CTLLSS74L16H501A"))
				.build();

		mvc.perform(post("/extraction/data")
				.queryParam("pmExtractionType", PMExtractionType.BPAY.toString())
				.content(CommonUtility.toJson(body))
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andReturn();
	} 
	
	@Test
	void pmDataExtraction_PPAL_OK() throws Exception {
		DataExtractionOptionsModel body = DataExtractionOptionsModel.builder()
				.creationDateFrom("2024-08-01")
				.creationDateTo("2024-08-31")
				.taxCodes(Arrays.asList("CTLLSS74L16H501A"))
				.build();

		mvc.perform(post("/extraction/data")
				.queryParam("pmExtractionType", PMExtractionType.PAYPAL.toString())
				.content(CommonUtility.toJson(body))
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(status().isOk())
		.andReturn();
	} 
	
	@Test
	void pmDataExtraction_ExtractionType_KO() throws Exception {
		DataExtractionOptionsModel body = DataExtractionOptionsModel.builder()
				.creationDateFrom("2024-08-01")
				.creationDateTo("2024-08-31")
				.taxCodes(Arrays.asList("CTLLSS74L16H501A"))
				.build();

		mvc.perform(post("/extraction/data")
				.queryParam("pmExtractionType", "MOCK")
				.content(CommonUtility.toJson(body))
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(status().isBadRequest())
		.andReturn();
	} 

}
