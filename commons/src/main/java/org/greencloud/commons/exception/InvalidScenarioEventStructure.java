package org.greencloud.commons.exception;

/**
 * Exception thrown for invalid scenario event structure
 */
public class InvalidScenarioEventStructure extends RuntimeException {

	public InvalidScenarioEventStructure(final String message) {
		super(String.format("Scenario event structure was invalid. Cause: %s", message));
	}
}
