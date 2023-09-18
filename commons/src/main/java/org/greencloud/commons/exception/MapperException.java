package org.greencloud.commons.exception;

public class MapperException extends RuntimeException {

	public MapperException(Exception exception) {
		super("Could not map a given value!", exception);
	}
}
