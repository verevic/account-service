package com.revolut.account.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Currency;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.revolut.ServiceException;
import com.revolut.account.dao.AccountOperationDAO;
import com.revolut.account.domain.Account;
import com.revolut.account.domain.AccountOperation;
import com.revolut.account.domain.AccountOwner;
import com.revolut.account.domain.Address;
import com.revolut.account.domain.Amount;
import com.revolut.account.service.AccountService;
import com.revolut.impl.db.AccountDBTest;

public class AccoutServiceImplTest extends AccountDBTest {
	private static AccountService service;

	@BeforeAll
	public static void setupAll() {
		service = new AccountServiceImpl(getFactory().getConnectionFactory(), getFactory().getAccountDAO(), getFactory().getAccountOperationDAO());
	}

	@Test
	public void testCreateAccount() throws SQLException, ServiceException {
		AccountOwner ao = new AccountOwner(-1, "Victor", new Address("Saburovo park"), "verevic@revolut.com");
		final AccountOwner owner;
		try (Connection c = getFactory().getConnectionFactory().getConnection()) {
			owner = getFactory().getAccountOwnerDAO().createOwner(c, ao);
			// New account
			BigDecimal amount = new BigDecimal("3.5E+5");
			Currency ccy = Currency.getInstance("RUB");
			Account account = service.createAccout(owner, new Amount(amount, ccy));
			Assertions.assertNotNull(account);
			Assertions.assertEquals(owner.getId(), account.getOwnerId());
			Assertions.assertEquals(0, amount.compareTo(account.getAmount().getAmount()));
			Assertions.assertEquals(ccy, account.getAmount().getCurrency());
			// AccountOperation
			AccountOperationDAO operationDAO = getFactory().getAccountOperationDAO();
			List<AccountOperation> operations = operationDAO.getOperationsFor(c, account);
			Assertions.assertNotNull(operations);
			Assertions.assertEquals(1, operations.size());
			AccountOperation op = operations.get(0);
			Assertions.assertTrue(op.getId() >= 0);
			Assertions.assertEquals(account.getId(), op.getAccountId());
			Assertions.assertNotNull(op.getTimestamp());
			Assertions.assertEquals("Account created", op.getDetails());

			Assertions.assertEquals(0, BigDecimal.ZERO.compareTo(op.getStartBalance().getAmount()));
			Assertions.assertEquals(ccy, op.getStartBalance().getCurrency());

			Assertions.assertEquals(0, amount.compareTo(op.getEndBalance().getAmount()));
			Assertions.assertEquals(ccy, op.getEndBalance().getCurrency());
		}
	}

	@Test
	public void testGetAccountsFor() throws SQLException, ServiceException {
		AccountOwner ao = new AccountOwner(-1, "Victor", new Address("Saburovo park"), "verevic@revolut.com");
		final AccountOwner owner;
		try (Connection c = getFactory().getConnectionFactory().getConnection()) {
			owner = getFactory().getAccountOwnerDAO().createOwner(c, ao);
		}

		List<Account> accounts = service.getAccountsFor(owner);
		Assertions.assertNotNull(accounts);
		Assertions.assertEquals(0, accounts.size());

		BigDecimal amount = new BigDecimal("3.5E+5");
		Currency ccy = Currency.getInstance("RUB");
		Account account = service.createAccout(owner, new Amount(amount, ccy));
		
		accounts = service.getAccountsFor(owner);
		Assertions.assertNotNull(accounts);
		Assertions.assertEquals(1, accounts.size());
		Assertions.assertEquals(account, accounts.get(0));
	}

	@Test
	public void testDeposit() throws SQLException, ServiceException {
		AccountOwner ao = new AccountOwner(-1, "Victor", new Address("Saburovo park"), "verevic@revolut.com");
		final AccountOwner owner;
		try (Connection c = getFactory().getConnectionFactory().getConnection()) {
			owner = getFactory().getAccountOwnerDAO().createOwner(c, ao);

			BigDecimal amount = new BigDecimal("3.5E+5");
			Currency ccy = Currency.getInstance("RUB");
			Account account = service.createAccout(owner, new Amount(amount, ccy));
	
			BigDecimal credit = new BigDecimal(123456);
			Account acc = service.deposit(account, new Amount(credit, ccy));
			Assertions.assertNotNull(acc);
			Assertions.assertEquals(account.getId(), acc.getId());
			BigDecimal expected = amount.add(credit);
			Assertions.assertEquals(0, expected.compareTo(acc.getAmount().getAmount()));
			// Operations
			AccountOperationDAO operationDAO = getFactory().getAccountOperationDAO();
			List<AccountOperation> operations = operationDAO.getOperationsFor(c, account);
			Assertions.assertNotNull(operations);
			Assertions.assertEquals(2, operations.size());
			// TODO: check operations
		}
	}

	@Test
	public void testWithdraw() throws SQLException, ServiceException {
		AccountOwner ao = new AccountOwner(-1, "Victor", new Address("Saburovo park"), "verevic@revolut.com");
		final AccountOwner owner;
		try (Connection c = getFactory().getConnectionFactory().getConnection()) {
			owner = getFactory().getAccountOwnerDAO().createOwner(c, ao);

			BigDecimal amount = new BigDecimal("3.5E+5");
			Currency ccy = Currency.getInstance("RUB");
			Account account = service.createAccout(owner, new Amount(amount, ccy));
	
			BigDecimal debit = new BigDecimal(123456);
			Account acc = service.withdraw(account, new Amount(debit, ccy));
			Assertions.assertNotNull(acc);
			Assertions.assertEquals(account.getId(), acc.getId());
			BigDecimal expected = amount.subtract(debit);
			Assertions.assertEquals(0, expected.compareTo(acc.getAmount().getAmount()));
			// Operations
			AccountOperationDAO operationDAO = getFactory().getAccountOperationDAO();
			List<AccountOperation> operations = operationDAO.getOperationsFor(c, account);
			Assertions.assertNotNull(operations);
			Assertions.assertEquals(2, operations.size());
			// TODO: check operations
		}
	}

	@Test
	public void testTransfer() throws SQLException, ServiceException {
		AccountOwner ao = new AccountOwner(-1, "Victor", new Address("Saburovo park"), "verevic@revolut.com");
		final AccountOwner owner;
		try (Connection c = getFactory().getConnectionFactory().getConnection()) {
			owner = getFactory().getAccountOwnerDAO().createOwner(c, ao);

			BigDecimal amount = new BigDecimal("3.5E+5");
			Currency ccy = Currency.getInstance("RUB");
			Account from = service.createAccout(owner, new Amount(amount, ccy));
			Account to = service.createAccout(owner, new Amount(BigDecimal.ZERO, ccy));
	
			service.transfer(from, to, new Amount(amount, ccy));
			List<Account> accounts = service.getAccountsFor(owner);
			Assertions.assertEquals(2, accounts.size());

			Account newFrom, newTo;
			if (accounts.get(0).getId() == from.getId()) {
				newFrom = accounts.get(0);
				newTo = accounts.get(1);
			} else {
				newFrom = accounts.get(1);
				newTo = accounts.get(0);
			}
			Assertions.assertEquals(0, BigDecimal.ZERO.compareTo(newFrom.getAmount().getAmount()));
			Assertions.assertEquals(0, amount.compareTo(newTo.getAmount().getAmount()));

			// Operations
			AccountOperationDAO operationDAO = getFactory().getAccountOperationDAO();
			List<AccountOperation> operations = operationDAO.getOperationsFor(c, from);
			Assertions.assertNotNull(operations);
			Assertions.assertEquals(2, operations.size());
			// TODO: check operations
			operations = operationDAO.getOperationsFor(c, to);
			Assertions.assertNotNull(operations);
			Assertions.assertEquals(2, operations.size());
			// TODO: check operations
		}
	}
}
