package com.revolut.exception;

import java.sql.SQLException;

@FunctionalInterface
public interface ServiceFunction<T, R> {
	R apply(T t) throws SQLException, BusinessRuleException;
}

