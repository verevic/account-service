package com.revolut.dao;

import java.sql.SQLException;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterEach;

import com.revolut.exception.BusinessRuleException;

import io.micronaut.test.annotation.MicronautTest;

// Not sure if micronaut injects static fields...
// That basically means I'll have to create/drop schema for each test...
// Might think of moving schema initialization to e.g. TransactionManager...

@MicronautTest
public class DatabaseTest {
	@Inject
	private TransactionManager transactionManager;

	@AfterEach
	public void clearDB() throws SQLException, BusinessRuleException {
		transactionManager.run(c -> {
			c.createStatement().execute("delete from AccountOperation");
			c.createStatement().execute("delete from Account");
			c.createStatement().execute("delete from AccountOwner");
		});
	}
}
