package org.greencloud.commons.exception;

/**
 * Exception thrown when invalid properties are passed to a given class
 */
public class InvalidPropertiesException extends RuntimeException {

	public InvalidPropertiesException(String message, Exception e) {
		super(message, e);
	}
}
