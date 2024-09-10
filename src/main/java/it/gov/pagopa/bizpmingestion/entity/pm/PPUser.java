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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "PP_USER")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PPUser {
	@Id
    @Column(name = "ID_USER", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name = "FISCAL_CODE")
	private String fiscalCode;
	@Column(name = "SURNAME")
	private String surname;
	@Column(name = "NAME")
	private String name;
	@Column(name = "NOTIFICATION_EMAIL", nullable = false)
	private String notificationEmail;
	
	@Builder.Default
    @OneToMany(targetEntity = PPTransaction.class, fetch = FetchType.LAZY, mappedBy="ppUser", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PPTransaction> ppTransaction = new ArrayList<>();

}
