package com.database.knowledge.exception;

public class InvalidGoalIdentifierException extends RuntimeException {

	public InvalidGoalIdentifierException(final int goalId) {
		super(String.format("Goal not found: Goal with identifier %d was not found", goalId));
	}
}
