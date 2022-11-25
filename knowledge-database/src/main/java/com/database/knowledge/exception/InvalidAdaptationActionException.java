package com.database.knowledge.exception;

public class InvalidAdaptationActionException extends RuntimeException {

	public InvalidAdaptationActionException(final String name) {
		super(String.format("Adaptation action not found: Adaptation action with name %s was not found", name));
	}
}