package org.greencloud.commons.exception;

import org.greencloud.commons.exception.domain.ExceptionMessages;

/**
 * Exception thrown when no weather is available in API
 */
public class APIFetchInternalException extends RuntimeException {

	public APIFetchInternalException() {
		super(ExceptionMessages.WEATHER_API_INTERNAL_ERROR);
	}
}
