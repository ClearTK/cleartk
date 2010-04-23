package org.cleartk;

public class CleartkRuntimeException extends RuntimeException {

	private static final long serialVersionUID = -4550657626985537362L;

	public CleartkRuntimeException() {
		super();
	}

	public CleartkRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public CleartkRuntimeException(String message) {
		super(message);
	}

	public CleartkRuntimeException(Throwable cause) {
		super(cause);
	}

}
