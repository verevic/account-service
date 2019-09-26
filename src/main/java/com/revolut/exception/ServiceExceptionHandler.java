package com.revolut.exception;

import javax.inject.Singleton;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.exceptions.ExceptionHandler;

@Produces
@Singleton
public class ServiceExceptionHandler implements ExceptionHandler<ServiceException, HttpResponse<ServiceExceptionError>> {

	@Override
	public HttpResponse<ServiceExceptionError> handle(@SuppressWarnings("rawtypes") HttpRequest request, ServiceException exception) {
		String msg = exception.getMessage();
		Throwable cause = exception.getCause();
		String reason = cause == null ? null : cause.getMessage();
		return HttpResponse.badRequest(new ServiceExceptionError(msg, reason));
	}

}
