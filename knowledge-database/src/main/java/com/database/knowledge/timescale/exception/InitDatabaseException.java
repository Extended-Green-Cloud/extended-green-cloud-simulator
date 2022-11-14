package com.database.knowledge.timescale.exception;

public class InitDatabaseException extends RuntimeException {

	public InitDatabaseException(Exception exception) {
		super("Error while initialization of the new database!", exception);
	}
}
