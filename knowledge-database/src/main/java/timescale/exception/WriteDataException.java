package timescale.exception;

public class WriteDataException extends RuntimeException {

	public WriteDataException(Exception exception) {
		super("Error while writing data to database!", exception);
	}
}
