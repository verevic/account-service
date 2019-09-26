package com.revolut.exception;

/**
 * Class to return an error description
 * 
 * @author verevic
 */
public class ServiceExceptionError {
	private final String msg;
	private final String reason;

	public ServiceExceptionError(String msg, String reason) {
		this.msg = msg;
		this.reason = reason;
	}

	public String getMsg() {
		return msg;
	}

	public String getReason() {
		return reason;
	}

	// Mutable class for deserialization
	public static class Builder {
		private String msg;
		private String reason;

		public void setMsg(String msg) {
			this.msg = msg;
		}
		public void setReason(String reason) {
			this.reason = reason;
		}

		public ServiceExceptionError build() {
			return new ServiceExceptionError(msg, reason);
		}
	}
}
