package org.greencloud.commons.exception;

import org.greencloud.commons.exception.domain.ExceptionMessages;

public class IncorrectTaskDateException extends RuntimeException {

	public IncorrectTaskDateException() {
		super(ExceptionMessages.INCORRECT_DATE_FORMAT);
	}
}
