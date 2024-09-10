package it.gov.pagopa.bizpmingestion.specification;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.domain.Specification;

import it.gov.pagopa.bizpmingestion.entity.pm.PPTransaction;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Root;

@SpringBootTest
class SpecificationTest {

	@Mock
	private CriteriaBuilder criteriaBuilderMock;

	@Mock
	private CriteriaQuery criteriaQueryMock;

	@Mock
	private Root<PPTransaction> ppTransactionMock;
	
	@Mock
	Path<Object> pathMock;
	
	private String creationDateFrom = "2024-08-01";
	private String creationDateTo   = "2024-08-31";


	@BeforeEach
	void setUp() {
		// precondition
		Join<Object, Object> joinMock = (Join<Object, Object>) mock(Join.class);
		when(ppTransactionMock.join(anyString(), eq(JoinType.INNER))).thenReturn(joinMock);
		when(joinMock.join(anyString(), eq(JoinType.INNER))).thenReturn(joinMock);
		
		when(joinMock.get(anyString())).thenReturn(pathMock);
		when(ppTransactionMock.get(anyString())).thenReturn(pathMock);
	}
	

	@ParameterizedTest
	@ValueSource(ints = {1, 2, 3})
	void checkExtractionSpec(int args) {
		
		Timestamp from = Timestamp.valueOf(LocalDate.parse(creationDateFrom, DateTimeFormatter.ISO_DATE).atStartOfDay());
		Timestamp to   = Timestamp.valueOf(LocalDate.parse(creationDateTo, DateTimeFormatter.ISO_DATE).atStartOfDay());
		
		Specification<PPTransaction> spec = new BPayExtractionSpec(creationDateFrom, creationDateTo, Arrays.asList("CTLLSS74L16H501A"));
		if (args == 2) {
			spec = new CardExtractionSpec(creationDateFrom, creationDateTo, Arrays.asList("CTLLSS74L16H501A"));
		} else if (args == 3) {
			spec = new PayPalExtractionSpec(creationDateFrom, creationDateTo, Arrays.asList("CTLLSS74L16H501A"));
		}
		spec.toPredicate(ppTransactionMock, criteriaQueryMock, criteriaBuilderMock);
		verify(ppTransactionMock, times(1)).get("status");
		verify(ppTransactionMock, times(1)).get("accountingStatus");
		verify(ppTransactionMock, times(1)).get("creationDate");
		
		verify(criteriaQueryMock, times(1)).distinct(true);
		verify(criteriaBuilderMock, times(1)).between((Expression)pathMock, from, to);
	}
}
