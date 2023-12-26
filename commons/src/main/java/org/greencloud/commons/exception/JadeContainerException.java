package org.greencloud.commons.exception;

/**
 * Exception thrown when JADE agent container could not be created
 */
public class JadeContainerException extends RuntimeException {

	public JadeContainerException(String message, Exception exception) {
		super(message, exception);
	}
}
