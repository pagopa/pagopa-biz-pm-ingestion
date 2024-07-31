package it.gov.pagopa.bizpmingestion.entity.pm;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PMEventPaymentDetail {
	private String iuv;
	private String enteBenificiario;
	private String idDomino;
	private String codicePagatore;
	private String nomePagatore;
}
