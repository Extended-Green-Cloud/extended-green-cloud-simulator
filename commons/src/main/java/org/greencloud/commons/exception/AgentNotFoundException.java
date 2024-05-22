package org.greencloud.commons.exception;

import static java.lang.String.format;

/**
 * Exception thrown when an agent was not found.
 */
public class AgentNotFoundException extends RuntimeException {

	public AgentNotFoundException(final String agent) {
		super(format("Agent %s was not found.", agent));
	}
}
