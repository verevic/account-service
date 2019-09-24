package com.revolut.account.dao;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Currency;
import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.revolut.account.domain.Account;
import com.revolut.account.domain.AccountOperation;
import com.revolut.account.domain.AccountOwner;
import com.revolut.account.domain.Address;
import com.revolut.account.domain.Amount;
import com.revolut.dao.DatabaseTest;
import com.revolut.dao.TransactionManager;

public class AccountOperationDAOTest extends DatabaseTest {
	@Inject
	private TransactionManager transactionManager;

	@Test
	public void testCreateOperation() throws SQLException {
		AccountOwner owner = transactionManager.runWithResult(c -> AccountOwnerDAO.createOwner(c,
				new AccountOwner(-1, "Victor", new Address("Saburovo park"), "verevic@revolut.com")));

		Currency ccy = Currency.getInstance("RUB");
		BigDecimal initial = new BigDecimal("12345678");
		Account account = transactionManager.runWithResult(c -> AccountDAO.createAccount(c, owner.getId(), new Amount(initial, ccy)));

		String details = "testCreateOperation";
		BigDecimal changed = new BigDecimal("87654321");
		AccountOperation operation = transactionManager.runWithResult(c -> AccountOperationDAO.createOperation(c,
				new AccountOperation(-1, account.getId(), null, details, new Amount(changed, ccy))));

		Assertions.assertNotNull(operation);
		Assertions.assertTrue(operation.getId() >= 0);
		Assertions.assertEquals(account.getId(), operation.getAccountId());
		Assertions.assertNotNull(operation.getTimestamp());
		Assertions.assertEquals(details, operation.getDetails());

		Assertions.assertEquals(0, changed.compareTo(operation.getBalance().getAmount()));
		Assertions.assertEquals(ccy, operation.getBalance().getCurrency());
	}

	@Test
	public void testGetOperationsFor() throws SQLException {
		AccountOwner owner = transactionManager.runWithResult(c -> AccountOwnerDAO.createOwner(c,
				new AccountOwner(-1, "Victor", new Address("Saburovo park"), "verevic@revolut.com")));

		Currency ccy = Currency.getInstance("RUB");
		BigDecimal initial = new BigDecimal("12345678");
		Account account = transactionManager.runWithResult(c -> AccountDAO.createAccount(c, owner.getId(), new Amount(initial, ccy)));

		String details = "testGetOperationsFor";
		BigDecimal changed = new BigDecimal("87654321");
		transactionManager.runWithResult(c -> AccountOperationDAO.createOperation(c,
			new AccountOperation(-1, account.getId(), null, details, new Amount(changed, ccy))));

		List<AccountOperation> operations = transactionManager.runWithResult(c -> AccountOperationDAO.getOperationsFor(c, account.getId()));
		Assertions.assertNotNull(operations);
		Assertions.assertEquals(1, operations.size());
		AccountOperation operation = operations.get(0);
		
		Assertions.assertNotNull(operation);
		Assertions.assertTrue(operation.getId() >= 0);
		Assertions.assertEquals(account.getId(), operation.getAccountId());
		Assertions.assertNotNull(operation.getTimestamp());
		Assertions.assertEquals(details, operation.getDetails());

		Assertions.assertEquals(0, changed.compareTo(operation.getBalance().getAmount()));
		Assertions.assertEquals(ccy, operation.getBalance().getCurrency());
	}
}
