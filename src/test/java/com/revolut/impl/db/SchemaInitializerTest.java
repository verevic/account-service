package com.revolut.impl.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.revolut.db.ConnectionFactory;

public class SchemaInitializerTest {
	private static ConnectionFactory factory = new ConnectionFactoryImpl();

	@AfterAll
	static void clearAll() throws SQLException {
		try (Connection c = factory.getConnection()) {
			c.createStatement().execute("drop table AccountOperation");
			c.createStatement().execute("drop table Account");
			c.createStatement().execute("drop table AccountOwner");
		}		
	}

	@Test
	public void testSchemaInitializer() {
		ConnectionFactory factory = new ConnectionFactoryImpl();
		try (Connection c = factory.getConnection()) {
			SchemaInitializer.setup(c);
		} catch (IOException | SQLException e) {
			Assertions.fail("Failed to initialize database", e);
		}
	}
}
