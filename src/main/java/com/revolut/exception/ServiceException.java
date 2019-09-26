package com.revolut.exception;

public class ServiceException extends Exception {
	private static final long serialVersionUID = 1L;

	public ServiceException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
