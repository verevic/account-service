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

import com.revolut.dao.JDBCUtils.ThrowableConsumer;
import com.revolut.exception.BusinessRuleException;
import com.revolut.exception.ServiceFunction;

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
// Use "shutdown=true" connection property to shutdown DB
	}

	public <R> R runWithResult(ServiceFunction<Connection, R> action) throws SQLException, BusinessRuleException {
		try (Connection c = ds.getConnection()) {
			c.setAutoCommit(false);
			try {
				R result = action.apply(c);
				c.commit();
				return result;
			} catch (SQLException | BusinessRuleException e) {
				c.rollback();
				throw e;
			} catch (Exception unexpected) {
				c.rollback();
				throw new SQLException("Caught an exception executing TransactionManager.runWithResult", unexpected);
			}
		}
	}

	public void run(ThrowableConsumer<Connection> action) throws SQLException, BusinessRuleException {
		try (Connection c = ds.getConnection()) {
			c.setAutoCommit(false);
			try {
				action.accept(c);
				c.commit();
			} catch (SQLException | BusinessRuleException e) {
				c.rollback();
				throw e;
			} catch (Exception unexpected) {
				c.rollback();
				throw new SQLException("Caught an exception executing TransactionManager.run", unexpected);
			}
		}
	}
}
