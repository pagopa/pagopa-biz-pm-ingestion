package it.gov.pagopa.bizpmingestion.entity.cosmos.view;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDetail implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -356906385523309670L;
	private String name;
	@NotBlank
	private String taxCode;
}
