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
@Table(name = "PP_PAYPAL")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PPPayPal {
	@Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "EMAIL_PP", nullable = false)
	private String emailPP;	
	
	@Column(name = "IS_DEFAULT", nullable = false)
	private Byte isDefault;	
	
	@Column(name = "FK_WALLET", nullable = false)
	private Long fkWallet;
	
	@ManyToOne(targetEntity = PPWallet.class, fetch = FetchType.LAZY)
	@JoinColumn(name = "FK_WALLET", referencedColumnName = "ID_WALLET", insertable = false, updatable = false)
	private PPWallet ppWallet;
}
