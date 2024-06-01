package org.greencloud.commons.exception;

/**
 * Exception thrown when content of messages exchanged between agents is incorrect
 */
public class AllocationAlgorithmNotFoundException extends RuntimeException {

	private static final String INCORRECT_ALGORITHM = "There is no allocation algorithm specified for a given data type!";

	public AllocationAlgorithmNotFoundException() {
		super(INCORRECT_ALGORITHM);
	}

}
