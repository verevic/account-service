package com.revolut.account.impl.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Currency;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.revolut.account.dao.AccountDAO;
import com.revolut.account.dao.AccountOwnerDAO;
import com.revolut.account.domain.Account;
import com.revolut.account.domain.AccountOwner;
import com.revolut.account.domain.Address;
import com.revolut.account.domain.Amount;
import com.revolut.impl.db.AccountDBTest;

public class JDBCAccountDAOTest extends AccountDBTest {
	private static AccountDAO dao;

	@BeforeAll
	public static void setupAll() {
		dao = new JDBCAccountDAO();
	}

	@Test
	public void testCreateAccount() throws SQLException {
		BigDecimal amount = new BigDecimal("3.5E+5");
		Currency ccy = Currency.getInstance("RUB");
		
		String name = "New owner";
		Address address = new Address("New owner address");
		String email = "new.owner@revolut.com";
		AccountOwner owner = new AccountOwner(-1, name, address, email);
		AccountOwnerDAO accountOwnerDAO = getFactory().getAccountOwnerDAO();

		final long ownerId;
		final Account account;
		try (Connection c = getFactory().getConnectionFactory().getConnection()) {
			AccountOwner storedOwner = accountOwnerDAO.createOwner(c, owner);
			ownerId = storedOwner.getId();

			account = dao.createAccount(c, storedOwner, new Amount(amount, ccy));
		}

		Assertions.assertNotNull(account);
		Assertions.assertTrue(account.getId() >= 0);
		Assertions.assertEquals(ownerId, account.getOwnerId());
		Assertions.assertEquals(amount, account.getAmount().getAmount().setScale(amount.scale()));
		Assertions.assertEquals(ccy, account.getAmount().getCurrency());
	}

	@Test
	public void testCredit() throws SQLException {
		BigDecimal amount = new BigDecimal("3.5E+5");
		BigDecimal credit = new BigDecimal("123456789");
		Currency ccy = Currency.getInstance("RUB");

		String name = "New owner";
		Address address = new Address("New owner address");
		String email = "new.owner@revolut.com";
		AccountOwner owner = new AccountOwner(-1, name, address, email);
		AccountOwnerDAO accountOwnerDAO = getFactory().getAccountOwnerDAO();

		final Account account;
		try (Connection c = getFactory().getConnectionFactory().getConnection()) {
			AccountOwner storedOwner = accountOwnerDAO.createOwner(c, owner);
			Account acc = dao.createAccount(c, storedOwner, new Amount(amount, ccy));
			account = dao.credit(c, acc, new Amount(credit, ccy));
		}

		Assertions.assertNotNull(account);
		BigDecimal expected = amount.add(credit);
		Assertions.assertEquals(0, expected.compareTo(account.getAmount().getAmount()));
	}

	@Test
	public void testDebit() throws SQLException {
		BigDecimal amount = new BigDecimal("3.5E+5");
		BigDecimal credit = new BigDecimal("123456789");
		Currency ccy = Currency.getInstance("RUB");

		String name = "New owner";
		Address address = new Address("New owner address");
		String email = "new.owner@revolut.com";
		AccountOwner owner = new AccountOwner(-1, name, address, email);
		AccountOwnerDAO accountOwnerDAO = getFactory().getAccountOwnerDAO();

		final Account account;
		try (Connection c = getFactory().getConnectionFactory().getConnection()) {
			AccountOwner storedOwner = accountOwnerDAO.createOwner(c, owner);
			Account acc = dao.createAccount(c, storedOwner, new Amount(amount, ccy));
			account = dao.debit(c, acc, new Amount(credit, ccy));
		}

		Assertions.assertNotNull(account);
		BigDecimal expected = amount.subtract(credit);
		Assertions.assertEquals(0, expected.compareTo(account.getAmount().getAmount()));
	}
}
