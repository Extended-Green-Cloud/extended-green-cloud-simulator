package org.greencloud.commons.exception;

import org.greencloud.commons.exception.domain.ExceptionMessages;

/**
 * Exception thrown when date of the task (job) execution is invalid
 */
public class IncorrectTaskDateException extends RuntimeException {

	public IncorrectTaskDateException() {
		super(ExceptionMessages.INCORRECT_DATE_FORMAT);
	}
}
