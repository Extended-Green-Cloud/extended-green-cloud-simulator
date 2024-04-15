package org.greencloud.commons.exception;

/**
 * Exception thrown when no weather is available in API
 */
public class APIFetchInternalException extends RuntimeException {

	private static final String WEATHER_API_INTERNAL_ERROR = "The API retrieved null instead of the weather data";

	public APIFetchInternalException() {
		super(WEATHER_API_INTERNAL_ERROR);
	}
}
