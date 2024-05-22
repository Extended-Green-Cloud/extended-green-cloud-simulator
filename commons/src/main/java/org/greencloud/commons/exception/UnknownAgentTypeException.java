package org.greencloud.commons.exception;

/**
 * Exception thrown when an agent is of unknown type.
 */
public class UnknownAgentTypeException extends RuntimeException {

	public UnknownAgentTypeException() {
		super("Unknown agent type.");
	}
}
