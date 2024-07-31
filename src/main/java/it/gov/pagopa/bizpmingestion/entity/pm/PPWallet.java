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
@Table(name = "PP_WALLET")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PPWallet {
	@Id
    @Column(name = "ID_WALLET", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "FK_CREDIT_CARD")
	private Long fkCreditCard;
	
	@ManyToOne(targetEntity = PPCreditCard.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "FK_CREDIT_CARD", referencedColumnName = "ID_CREDIT_CARD", insertable = false, updatable = false)
	private PPCreditCard ppCreditCard;
	
}
