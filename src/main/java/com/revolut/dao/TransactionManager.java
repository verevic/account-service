package com.revolut.dao;

import java.sql.Connection;
import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolut.dao.JDBCUtils.SQLFunction;
import com.revolut.dao.JDBCUtils.ThrowableConsumer;

@Singleton
public class TransactionManager {
	private static final Logger log = LoggerFactory.getLogger(TransactionManager.class);

	private final DataSource ds;

	@Inject
	public TransactionManager(DataSource dataSource) {
		this.ds = dataSource;
	}

	@PostConstruct
	public void onCreated() {
		// setup the schema
		try (Connection c = ds.getConnection()) {
			SchemaInitializer.setup(c);
			log.info("TransactionManager created");
		} catch (SQLException e) {
			log.error("Failed to setup DB schema", e);
		}
	}

	@PreDestroy
	public void onDestroying() {
		log.info("TransactionManager's being destroyed");
// Use "shutdown=true" connection property instead
//		try (Connection c = ds.getConnection()) {
//			c.createStatement().execute("SHUTDOWN");
//		} catch (SQLException e) {
//			log.error("Failed to clear DB", e);
//		}
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
