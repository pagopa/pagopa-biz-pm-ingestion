package it.gov.pagopa.bizpmingestion.entity.pm;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "PP_PAYMENT_DETAIL")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PPPaymentDetail {
	@Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "PAYMENT_ID", nullable = false)
	private String paymentId;
	@Column(name = "IUV", nullable = false)
	private String iuv;
	
	@Column(name = "ENTE_BENEFICIARIO")
	private String enteBeneficiario;
	@Column(name = "ID_DOMINIO")
	private String idDominio; // CF EC
	@Column(name = "CODICE_PAGATORE")
	private String codicePagatore;
	@Column(name = "NOME_PAGATORE")
	private String nomePagatore;
	
	
	@ManyToOne(targetEntity = PPPayment.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "PAYMENT_ID", referencedColumnName = "ID", insertable = false, updatable = false)
	private PPPayment ppPayment;	
}
