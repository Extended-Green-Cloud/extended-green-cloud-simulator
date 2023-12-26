package org.greencloud.commons.exception;

/**
 * Exception throw for invalid scenario definition
 */
public class InvalidScenarioException extends RuntimeException {

	public InvalidScenarioException(String message, Exception e) {
		super(message, e);
	}
}
