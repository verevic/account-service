package com.revolut.dao;

import java.sql.SQLException;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import io.micronaut.test.annotation.MicronautTest;

// Not sure if micronaut injects static fields...
// That basically means I'll have to create/drop schema for each test...
// Might think of moving schema initialization to e.g. TransactionManager...

@MicronautTest
public class DatabaseTest {
	@Inject
	private TransactionManager transactionManager;

	@BeforeEach
	public void dbSetup() throws SQLException {
		transactionManager.run(SchemaInitializer::setup);
	}

	@AfterEach
	public void clearDB() throws SQLException {
		transactionManager.run(c -> {
//			c.createStatement().execute("delete from AccountOperation");
//			c.createStatement().execute("delete from Account");
//			c.createStatement().execute("delete from AccountOwner");
			c.createStatement().execute("drop table AccountOperation");
			c.createStatement().execute("drop table Account");
			c.createStatement().execute("drop table AccountOwner");
		});
	}

//	@AfterAll
//	public static void dbClearAll() throws SQLException {
//		transactionManager.run(c -> {
//			c.createStatement().execute("drop table AccountOperation");
//			c.createStatement().execute("drop table Account");
//			c.createStatement().execute("drop table AccountOwner");
//		});
//	}
}
