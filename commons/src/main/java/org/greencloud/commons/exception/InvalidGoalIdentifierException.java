package org.greencloud.commons.exception;

import static java.lang.String.format;

/**
 * Exception thrown when invalid adaptation goal is passed
 */
public class InvalidGoalIdentifierException extends RuntimeException {

	public InvalidGoalIdentifierException(final int goalId) {
		super(format("Goal not found: Goal with identifier %d was not found", goalId));
	}
}
