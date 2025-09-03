package org.greencloud.commons.exception;

/**
 * Exception throw for invalid configuration definition
 */
public class InvalidConfigurationException extends RuntimeException {

	public InvalidConfigurationException(String message, Exception e) {
		super(message, e);
	}
}
