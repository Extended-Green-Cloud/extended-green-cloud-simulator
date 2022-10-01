package com.greencloud.application.exception;

import static com.greencloud.application.exception.domain.ExceptionMessages.INCORRECT_MESSAGE_FORMAT;

import java.io.Serial;

public class IncorrectMessageContentException extends RuntimeException{

	@Serial
	private static final long serialVersionUID = -6101862598975601339L;

	public IncorrectMessageContentException() {
		super(INCORRECT_MESSAGE_FORMAT);
	}

}
