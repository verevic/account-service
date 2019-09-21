package com.revolut;

import com.revolut.db.ConnectionFactory;

public interface BeanFactory {
	ConnectionFactory getConnectionFactory();
}
