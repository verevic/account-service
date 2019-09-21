package com.revolut.impl.db;

import java.sql.Connection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.revolut.db.ConnectionFactory;

public class ConnectionFactoryTest {
	@Test
	public void testGetConnection() throws Exception {
		ConnectionFactory factory = new ConnectionFactoryImpl();
		try (Connection connection = factory.getConnection()) {
			Assertions.assertNotNull(connection);
		}
	}
}
