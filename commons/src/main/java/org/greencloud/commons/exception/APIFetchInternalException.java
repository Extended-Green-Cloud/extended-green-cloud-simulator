package org.greencloud.commons.exception;

import org.greencloud.commons.exception.domain.ExceptionMessages;

public class APIFetchInternalException extends RuntimeException {

	public APIFetchInternalException() {
		super(ExceptionMessages.WEATHER_API_INTERNAL_ERROR);
	}
}
