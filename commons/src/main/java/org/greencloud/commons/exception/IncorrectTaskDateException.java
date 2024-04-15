package org.greencloud.commons.exception;

/**
 * Exception thrown when date of the task (job) execution is invalid
 */
public class IncorrectTaskDateException extends RuntimeException {

	private static final String INCORRECT_DATE_FORMAT = "The provided execution date has incorrect format";

	public IncorrectTaskDateException() {
		super(INCORRECT_DATE_FORMAT);
	}
}
