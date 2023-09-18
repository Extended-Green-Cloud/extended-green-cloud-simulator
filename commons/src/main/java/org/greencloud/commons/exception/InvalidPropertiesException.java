package org.greencloud.commons.exception;

public class InvalidPropertiesException extends RuntimeException {

	public InvalidPropertiesException(String message, Exception e) {
		super(message, e);
	}
}
