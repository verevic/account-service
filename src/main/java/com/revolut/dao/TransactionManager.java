package com.revolut.dao;

import java.sql.Connection;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

import com.revolut.dao.JDBCUtils.SQLFunction;
import com.revolut.dao.JDBCUtils.ThrowableConsumer;

@Singleton
public class TransactionManager {
	private final DataSource ds;

	@Inject
	public TransactionManager(DataSource dataSource) {
		this.ds = dataSource;
	}

	public <R> R runWithResult(SQLFunction<Connection, R> action) throws SQLException {
		try (Connection c = ds.getConnection()) {
			c.setAutoCommit(false);
			try {
				R result = action.apply(c);
				c.commit();
				return result;
			} catch (SQLException e) {
				c.rollback();
				throw e;
			} catch (Exception e) {
				c.rollback();
				throw new SQLException("Caught an exception executing TransactionManager.run", e);
			}
		}
	}

	public void run(ThrowableConsumer<Connection> action) throws SQLException {
		try (Connection c = ds.getConnection()) {
			c.setAutoCommit(false);
			try {
				action.accept(c);
				c.commit();
			} catch (SQLException e) {
				c.rollback();
				throw e;
			} catch (Exception e) {
				c.rollback();
				throw new SQLException("Caught an exception executing TransactionManager.run", e);
			}
		}
	}
}
