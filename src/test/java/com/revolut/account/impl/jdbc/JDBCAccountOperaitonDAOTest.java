package com.revolut.account.impl.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Currency;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.revolut.account.dao.AccountDAO;
import com.revolut.account.dao.AccountOperationDAO;
import com.revolut.account.dao.AccountOwnerDAO;
import com.revolut.account.domain.Account;
import com.revolut.account.domain.AccountOperation;
import com.revolut.account.domain.AccountOwner;
import com.revolut.account.domain.Address;
import com.revolut.account.domain.Amount;
import com.revolut.impl.db.AccountDBTest;

public class JDBCAccountOperaitonDAOTest extends AccountDBTest {
	private static AccountOperationDAO dao;

	@BeforeAll
	public static void setupAll() {
		dao = new JDBCAccountOperationDAO();
	}

	@Test
	public void testCreateOperation() throws SQLException {
		AccountOwnerDAO ownerDAO = getFactory().getAccountOwnerDAO();
		AccountDAO accountDAO = getFactory().getAccountDAO();

		Currency ccy = Currency.getInstance("RUB");
		BigDecimal initial = new BigDecimal("12345678");
		BigDecimal changed = new BigDecimal("87654321");

		String details = "testCreateOperation";
		final long accId;
		final AccountOperation operation;
		try (Connection c = getFactory().getConnectionFactory().getConnection()) {
			AccountOwner owner = ownerDAO.createOwner(c, new AccountOwner(-1, "Victor", new Address("Saburovo park"), "verevic@revolut.com"));
			Account account = accountDAO.createAccount(c, owner, new Amount(initial, ccy));
			Assertions.assertNotNull(account);
			accId = account.getId();

			operation = dao.createOperation(c, new AccountOperation(-1, accId, null, details,
					new Amount(initial, ccy), new Amount(changed, ccy)));
		}

		Assertions.assertNotNull(operation);
		Assertions.assertTrue(operation.getId() >= 0);
		Assertions.assertEquals(accId, operation.getAccountId());
		Assertions.assertNotNull(operation.getTimestamp());
		Assertions.assertEquals(details, operation.getDetails());

		Assertions.assertEquals(0, initial.compareTo(operation.getStartBalance().getAmount()));
		Assertions.assertEquals(ccy, operation.getStartBalance().getCurrency());

		Assertions.assertEquals(0, changed.compareTo(operation.getEndBalance().getAmount()));
		Assertions.assertEquals(ccy, operation.getEndBalance().getCurrency());
	}

	@Test
	public void testGetOperationsFor() throws SQLException {
		AccountOwnerDAO ownerDAO = getFactory().getAccountOwnerDAO();
		AccountDAO accountDAO = getFactory().getAccountDAO();

		Currency ccy = Currency.getInstance("RUB");
		BigDecimal initial = new BigDecimal("12345678");
		BigDecimal changed = new BigDecimal("87654321");

		String details = "testGetOperationsFor";
		final long accId;
		final List<AccountOperation> operations;
		try (Connection c = getFactory().getConnectionFactory().getConnection()) {
			AccountOwner owner = ownerDAO.createOwner(c, new AccountOwner(-1, "Victor", new Address("Saburovo park"), "verevic@revolut.com"));
			Account account = accountDAO.createAccount(c, owner, new Amount(initial, ccy));
			Assertions.assertNotNull(account);
			accId = account.getId();
			dao.createOperation(c, new AccountOperation(-1, accId, null, details,
					new Amount(initial, ccy), new Amount(changed, ccy)));
			operations = dao.getOperationsFor(c, account);
		}

		Assertions.assertNotNull(operations);
		Assertions.assertEquals(1, operations.size());
		AccountOperation operation = operations.get(0);
		
		Assertions.assertNotNull(operation);
		Assertions.assertTrue(operation.getId() >= 0);
		Assertions.assertEquals(accId, operation.getAccountId());
		Assertions.assertNotNull(operation.getTimestamp());
		Assertions.assertEquals(details, operation.getDetails());

		Assertions.assertEquals(0, initial.compareTo(operation.getStartBalance().getAmount()));
		Assertions.assertEquals(ccy, operation.getStartBalance().getCurrency());

		Assertions.assertEquals(0, changed.compareTo(operation.getEndBalance().getAmount()));
		Assertions.assertEquals(ccy, operation.getEndBalance().getCurrency());
	}
}
