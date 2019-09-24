package com.revolut.dao;

import java.sql.SQLException;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import io.micronaut.test.annotation.MicronautTest;

@MicronautTest
public class SchemaInitializerTest {
	@Inject
	private TransactionManager instTransactionManager;

	@AfterEach
	public void clearll() throws SQLException {
		instTransactionManager.run(c -> {
			c.createStatement().execute("drop table AccountOperation");
			c.createStatement().execute("drop table Account");
			c.createStatement().execute("drop table AccountOwner");
		});
	}

	@Test
	public void testSchemaInitializer() throws SQLException {
		instTransactionManager.run(SchemaInitializer::setup);
	}
}
