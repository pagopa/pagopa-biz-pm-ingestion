package it.gov.pagopa.bizpmingestion.entity.pm;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PP_USER")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PPTransaction {
	@Id
    @Column(name = "ID", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "RRN")
	private String rrn;
	@Column(name = "NUMAUT")
	private String numAut;
	@Column(name = "CREATION_DATE")
	private Timestamp creationDate;
	@Column(name = "AMOUNT")
	private Long amount;
	@Column(name = "FEE")
	private Long fee;
	@Column(name = "GRAND_TOTAL")
	private Long grandTotal;
	@Column(name = "SERVICE_NAME")
	private String serviceName;
	@Column(name = "STATUS")
	private Byte status;
	@Column(name = "ACCOUNTING_STATUS")
	private Byte accountingStatus;
	
	@Column(name = "FK_USER", nullable = false)
	private Long fkUser;
	@Column(name = "FK_PAYMENT", nullable = false)
	private Long fkPayment;
	@Column(name = "FK_WALLET", nullable = false)
	private Long fkWallet;
	@Column(name = "FK_PSP", nullable = false)
	private Long fkPsp;
	
	@ManyToOne
	@JoinColumn(name = "ID_USER")
	private PPUser ppUser;
	

}
