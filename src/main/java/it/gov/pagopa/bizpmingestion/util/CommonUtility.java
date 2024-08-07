package it.gov.pagopa.bizpmingestion.util;

import java.util.Calendar;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommonUtility {

	/**
	 * @param value value to deNullify.
	 * @return return empty string if value is null
	 */
	public static String deNull(String value) {
		return Optional.ofNullable(value).orElse("");
	}

	/**
	 * @param value value to deNullify.
	 * @return return empty string if value is null
	 */
	public static String deNull(Object value) {
		return Optional.ofNullable(value).orElse("").toString();
	}

	/**
	 * @param value value to deNullify.
	 * @return return false if value is null
	 */
	public static Boolean deNull(Boolean value) {
		return Optional.ofNullable(value).orElse(false);
	}

	/**
	 * @param headers header of the CSV file
	 * @param rows    data of the CSV file
	 * @return byte array of the CSV using commas (;) as separator
	 */
	public static byte[] createCsv(List<String> headers, List<List<String>> rows) {
		var csv = new StringBuilder();
		csv.append(String.join(";", headers));
		rows.forEach(row -> csv.append(System.lineSeparator()).append(String.join(";", row)));
		return csv.toString().getBytes();
	}

	public static long getTimelapse(long startTime) {
		return Calendar.getInstance().getTimeInMillis() - startTime;
	}
	
	/**
     * @param object to map into the Json string
     * @return object as Json string
     * @throws JsonProcessingException if there is an error during the parsing of the object
     */
    public static String toJson(Object object) throws JsonProcessingException {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                .writeValueAsString(object);
    }

}
