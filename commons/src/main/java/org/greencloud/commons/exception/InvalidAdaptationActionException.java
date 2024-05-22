package org.greencloud.commons.exception;

import static java.lang.String.format;

/**
 * Exception thrown when incorrect adaptation action has been passed
 */
public class InvalidAdaptationActionException extends RuntimeException {

	public InvalidAdaptationActionException(final String name) {
		super(format("Adaptation action not found: Adaptation action with name %s was not found", name));
	}
}
