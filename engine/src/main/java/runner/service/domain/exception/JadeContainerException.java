package runner.service.domain.exception;

public class JadeContainerException extends RuntimeException {

	public JadeContainerException(String message, Exception exception) {
		super(message, exception);
	}
}
