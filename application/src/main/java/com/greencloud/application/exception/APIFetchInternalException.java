package com.greencloud.application.exception;

import static com.greencloud.application.exception.domain.ExceptionMessages.WEATHER_API_INTERNAL_ERROR;

import java.io.Serial;

public class APIFetchInternalException extends RuntimeException{
	@Serial
	private static final long serialVersionUID = 5295657283189216960L;

	public APIFetchInternalException() {
		super(WEATHER_API_INTERNAL_ERROR);
	}
}
