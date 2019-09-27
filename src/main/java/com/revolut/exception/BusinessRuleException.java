package com.revolut.exception;

@SuppressWarnings("serial")
public class BusinessRuleException extends Exception {
	public BusinessRuleException(String reason) {
		super(reason);
	}
}
