package org.greencloud.managingsystem.exception;

public class DatabaseConnectionNotAvailable extends RuntimeException {

	public DatabaseConnectionNotAvailable(final String msg) {
		super(String.join(":", msg, " Database connection is not available because agent node does not exist"));
	}
}
