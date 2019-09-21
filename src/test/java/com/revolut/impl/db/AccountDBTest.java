package com.revolut.impl.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

import com.revolut.BeanFactory;
import com.revolut.account.AccountBeanFactory;
import com.revolut.account.impl.AccountBeanFactoryImpl;

public class AccountDBTest {
	private static AccountBeanFactory factory;
	@BeforeAll
	public static void dbSetupAll() throws SQLException, IOException {
		BeanFactory beanFactory = new TestBeanFactoryImpl();
		try (Connection c = beanFactory.getConnectionFactory().getConnection()) {
			SchemaInitializer.setup(c);
		}
		factory = new AccountBeanFactoryImpl(beanFactory);
	}

	public static AccountBeanFactory getFactory() {
		return factory;
	}

	@AfterEach
	public void clearDB() throws SQLException {
		try (Connection c = factory.getConnectionFactory().getConnection()) {
			c.createStatement().execute("delete from AccountOperation");
			c.createStatement().execute("delete from Account");
			c.createStatement().execute("delete from AccountOwner");
		}		
	}

	@AfterAll
	public static void dbClearAll() throws SQLException {
		try (Connection c = factory.getConnectionFactory().getConnection()) {
			c.createStatement().execute("drop table AccountOperation");
			c.createStatement().execute("drop table Account");
			c.createStatement().execute("drop table AccountOwner");
		}		
	}
}
