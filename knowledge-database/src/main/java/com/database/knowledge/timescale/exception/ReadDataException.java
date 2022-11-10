package com.database.knowledge.timescale.exception;

public class ReadDataException extends RuntimeException {

	public ReadDataException(Exception exception) {
		super("Error while reading from database!", exception);
	}
}
