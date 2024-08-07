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
@Table(name = "PP_BPAY")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PPBPay {
	@Id
    @Column(name = "ID_BPAY", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "CELLPHONE_NUMBER")
	private String cellphoneNumber;	
}
