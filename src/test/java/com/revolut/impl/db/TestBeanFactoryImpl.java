package com.revolut.impl.db;

import com.revolut.BeanFactory;
import com.revolut.db.ConnectionFactory;

public class TestBeanFactoryImpl implements BeanFactory {
	@Override
	public ConnectionFactory getConnectionFactory() {
		return ConnectionFactoryHolder.instance;
	}

	private static class ConnectionFactoryHolder {
		private final static ConnectionFactory instance = new ConnectionFactoryImpl();
	}
}
