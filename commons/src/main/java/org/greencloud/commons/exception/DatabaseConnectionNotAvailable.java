package org.greencloud.commons.exception;

import static java.lang.String.join;

/**
 * Exception thrown when the database connection cannot be established
 */
public class DatabaseConnectionNotAvailable extends RuntimeException {

	public DatabaseConnectionNotAvailable(final String msg) {
		super(join(":", msg, " Database connection is not available"));
	}
}
