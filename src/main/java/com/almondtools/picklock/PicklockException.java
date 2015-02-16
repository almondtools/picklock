package com.almondtools.picklock;

public class PicklockException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PicklockException() {
	}

	public PicklockException(String message, Throwable cause) {
		super(message, cause);
	}

	public PicklockException(String message) {
		super(message);
	}

	public PicklockException(Throwable cause) {
		super(cause);
	}

}
