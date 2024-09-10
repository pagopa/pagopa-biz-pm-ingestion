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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
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
	
	@Column(name = "TYPE")
	private Byte type;
	
	@Column(name = "FK_CREDIT_CARD")
	private Long fkCreditCard;
	
	@Column(name = "FK_BPAY")
	private Long fkBPay;
	
	@ManyToOne(targetEntity = PPCreditCard.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "FK_CREDIT_CARD", referencedColumnName = "ID_CREDIT_CARD", insertable = false, updatable = false)
	private PPCreditCard ppCreditCard;
	
	@ManyToOne(targetEntity = PPBPay.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "FK_BPAY", referencedColumnName = "ID_BPAY", insertable = false, updatable = false)
	private PPBPay ppBPay;
	
	@Builder.Default
	@OneToMany(targetEntity = PPPayPal.class, fetch = FetchType.LAZY, mappedBy="ppWallet", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PPPayPal> ppPayPal = new ArrayList<>();
}
