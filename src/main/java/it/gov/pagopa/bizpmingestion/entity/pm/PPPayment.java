package it.gov.pagopa.bizpmingestion.entity.pm;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "PP_PAYMENT")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PPPayment {
	@Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "ID_PAYMENT", nullable = false)
	private String idPayment;
	
	@Column(name = "ID_CARRELLO")
	private String idCarrello;
	
	@Column(name = "FISCAL_CODE")
	private String fiscalCode; // CF debitore
	@Column(name = "RECEIVER")
	private String receiver; // ente creditore
	@Column(name = "SUBJECT")
	private String subject; // oggetto pagamento
	@Column(name = "ORIGIN")
	private String origin; // canale pagamento
	
	@Builder.Default
	@OneToMany(targetEntity = PPPaymentDetail.class, fetch = FetchType.LAZY, mappedBy = "ppPayment",  cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PPPaymentDetail> ppPaymentDetail = new ArrayList<>();
	
}
