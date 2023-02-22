package com.database.knowledge.exception;

public class DatabaseConnectionNotAvailable extends RuntimeException {

	public DatabaseConnectionNotAvailable(final String msg) {
		super(String.join(":", msg, " Database connection is not available"));
	}
}
