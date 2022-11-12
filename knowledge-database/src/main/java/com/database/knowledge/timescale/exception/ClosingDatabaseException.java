package com.database.knowledge.timescale.exception;

public class ClosingDatabaseException extends RuntimeException {

	public ClosingDatabaseException(Exception exception) {
		super("Error while closing connection to the database!", exception);
	}
}
