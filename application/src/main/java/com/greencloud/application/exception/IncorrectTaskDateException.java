package com.greencloud.application.exception;

import static com.greencloud.application.exception.domain.ExceptionMessages.INCORRECT_DATE_FORMAT;

import java.io.Serial;

public class IncorrectTaskDateException extends RuntimeException {

	@Serial
	private static final long serialVersionUID = -3780619229831674878L;

	public IncorrectTaskDateException() {
		super(INCORRECT_DATE_FORMAT);
	}
}
