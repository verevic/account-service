package com.revolut.impl.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.revolut.BeanFactory;
import com.revolut.db.ConnectionFactory;

public class SchemaInitializingBeanFactoryImpl implements BeanFactory {
	private final static Logger log = LoggerFactory.getLogger(SchemaInitializingBeanFactoryImpl.class);

	@Override
	public ConnectionFactory getConnectionFactory() {
		return ConnectionFactoryHolder.instance;
	}

	private static class ConnectionFactoryHolder {
		private final static ConnectionFactory instance;
		static {
			ConnectionFactory factory = new ConnectionFactoryImpl();
			try (Connection c = factory.getConnection()) {
				SchemaInitializer.setup(c);
			} catch (IOException | SQLException e) {
				factory = null;
				log.error("Caught an exception initializing database", e);
			}
			instance = factory;
		}
	}
}
