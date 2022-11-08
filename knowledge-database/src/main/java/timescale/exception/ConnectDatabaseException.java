package timescale.exception;

public class ConnectDatabaseException extends RuntimeException {

	public ConnectDatabaseException(Exception exception) {
		super("Error while connecting to database!", exception);
	}
}
