package org.greencloud.managingsystem.service.executor.jade;

public class JadeControllerException extends RuntimeException {

	public JadeControllerException(String message, Exception exception) {
		super(message, exception);
	}
}
