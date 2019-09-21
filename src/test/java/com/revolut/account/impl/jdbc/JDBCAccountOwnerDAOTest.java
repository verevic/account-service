package com.revolut.account.impl.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Currency;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.revolut.account.dao.AccountDAO;
import com.revolut.account.dao.AccountOwnerDAO;
import com.revolut.account.domain.Account;
import com.revolut.account.domain.AccountOwner;
import com.revolut.account.domain.Address;
import com.revolut.account.domain.Amount;
import com.revolut.impl.db.AccountDBTest;

public class JDBCAccountOwnerDAOTest extends AccountDBTest {
	private static AccountOwnerDAO dao;

	@BeforeAll
	public static void setupAll() {
		dao = new JDBCAccountOwnerDAO();
	}

	static Object[][] createOwnerParams() {
		return new Object[][] {
			new Object[] {"Victor", new Address("Saburovo park"), "verevic@revolut.com"},
			new Object[] {"Victor", new Address(null), "verevic@revolut.com"},
			new Object[] {"Victor", null, "verevic@revolut.com"},
			new Object[] {"Victor", "Saburovo park", null},
			new Object[] {"Victor", null, null},
		};
	}

	@ParameterizedTest
	@MethodSource("createOwnerParams")
	public void testCreateOwner(String name, Address address, String email) throws SQLException {
		AccountOwner detached = new AccountOwner(-1, name, address, email);

		final AccountOwner stored;
		try (Connection c = getFactory().getConnectionFactory().getConnection()) {
			 stored = dao.createOwner(c, detached);
		}
		Assertions.assertNotNull(stored);
		Assertions.assertNotEquals(-1, stored.getId());
		Assertions.assertEquals(name, stored.getName());
		if (address == null || address.getAddress() == null)
			Assertions.assertNull(stored.getAddress());
		else
			Assertions.assertEquals(address.getAddress(), stored.getAddress().getAddress());
		Assertions.assertEquals(email, stored.getEmail());
	}

	@Test
	public void testGetOwners() throws SQLException {
		String name = "New owner";
		Address address = new Address("New owner address");
		String email = "new.owner@revolut.com";
		AccountOwner owner = new AccountOwner(-1, name, address, email);

		final List<AccountOwner> oldOwners;
		final List<AccountOwner> newOwners;
		final long ownerId;
		try (Connection c = getFactory().getConnectionFactory().getConnection()) {
			oldOwners = dao.getOwners(c);
			AccountOwner newOwner = dao.createOwner(c, owner);
			ownerId = newOwner.getId();
			newOwners = dao.getOwners(c);
		}

		Assertions.assertNotNull(oldOwners);
		for (AccountOwner o : oldOwners) {
			if (o.getId() == ownerId) {
				Assertions.fail("Owner should not be there yet");
			}
		}

		Assertions.assertNotNull(newOwners);
		boolean found = false;
		for (AccountOwner o : newOwners) {
			if (o.getId() == ownerId) {
				found = true;
				Assertions.assertEquals(name, o.getName());
				Assertions.assertEquals(address.getAddress(), o.getAddress().getAddress());
				Assertions.assertEquals(email, o.getEmail());
			}
		}
		Assertions.assertTrue(found);
	}

	@Test
	public void testGetAccounts() throws SQLException {
		BigDecimal amount = new BigDecimal("3.5E+5");
		Currency ccy = Currency.getInstance("RUB");
		
		AccountOwner owner = new AccountOwner(-1, "An owner", new Address("Address"), "email");
		AccountDAO accountDAO = getFactory().getAccountDAO();

		final long accountId;
		final List<Account> accountsBefore;
		final List<Account> accountsAfter;
		try (Connection c = getFactory().getConnectionFactory().getConnection()) {
			AccountOwner storedOwner = dao.createOwner(c, owner);
			accountsBefore = accountDAO.getAccounts(c, storedOwner);
			Assertions.assertNotNull(accountsBefore);

			Account account = accountDAO.createAccount(c, storedOwner, new Amount(amount, ccy));
			Assertions.assertNotNull(account);
			accountId = account.getId();
			Assertions.assertTrue(accountId >= 0);

			accountsAfter = accountDAO.getAccounts(c, storedOwner);
			Assertions.assertNotNull(accountsAfter);
		}

		Assertions.assertEquals(accountsBefore.size() + 1, accountsAfter.size());
		boolean found = false;
		for (Account acc: accountsAfter) {
			if (acc.getId() == accountId) {
				found = true;
			}
		}
		Assertions.assertTrue(found);
	}
}
