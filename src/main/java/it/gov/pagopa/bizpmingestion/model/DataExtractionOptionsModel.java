package it.gov.pagopa.bizpmingestion.model;

import java.io.Serializable;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DataExtractionOptionsModel implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 8329148883733208789L;
	
	private List<String> taxCodes;
	@Schema(description = "if provided use the format yyyy-MM-dd")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	@JsonFormat(pattern = "yyyy-MM-dd")
    private String creationDateFrom;
	@Schema(description = "if provided use the format yyyy-MM-dd")
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	@JsonFormat(pattern = "yyyy-MM-dd")
    private String creationDateTo;
}
