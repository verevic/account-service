package com.revolut.dao;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.inject.Inject;
import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import io.micronaut.test.annotation.MicronautTest;

@MicronautTest
public class SchemaInitializerTest {
	@Inject
	private DataSource ds;

	@AfterEach
	public void clearll() throws SQLException {
		try(Connection c = ds.getConnection()) {
			c.createStatement().execute("drop table AccountOperation");
			c.createStatement().execute("drop table Account");
			c.createStatement().execute("drop table AccountOwner");
		};
	}

	@Test
	public void testSchemaInitializer() throws SQLException, IOException {
		try(Connection c = ds.getConnection()) {
			SchemaInitializer.setup(c);
		}
	}
}
