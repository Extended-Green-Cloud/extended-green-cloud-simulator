package org.greencloud.commons.exception;

/**
 * Exception thrown when problem appears on the mapper level
 */
public class MapperException extends RuntimeException {

	public MapperException(Exception exception) {
		super("Could not map a given value!", exception);
	}
}
