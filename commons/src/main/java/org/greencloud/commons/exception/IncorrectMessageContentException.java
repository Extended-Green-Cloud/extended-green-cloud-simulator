package org.greencloud.commons.exception;

public class IncorrectMessageContentException extends RuntimeException {

	public static final String INCORRECT_MESSAGE_FORMAT = "The provided message content has incorrect format";

	public IncorrectMessageContentException() {
		super(INCORRECT_MESSAGE_FORMAT);
	}

}
