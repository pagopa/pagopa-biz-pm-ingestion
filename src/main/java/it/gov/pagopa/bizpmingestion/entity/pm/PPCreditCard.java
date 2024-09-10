package it.gov.pagopa.bizpmingestion.entity.pm;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "PP_CREDIT_CARD")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PPCreditCard {
	@Id
    @Column(name = "ID_CREDIT_CARD", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "CARD_NUMBER")
	private String cardNumber;
	
	@Column(name = "VPOS_CIRCUIT_CODE")
	private String vposCircuitCode;
	
}
