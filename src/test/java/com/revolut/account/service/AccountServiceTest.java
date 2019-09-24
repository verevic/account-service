package com.revolut.account.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Currency;
import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.revolut.ServiceException;
import com.revolut.account.dao.AccountOperationDAO;
import com.revolut.account.dao.AccountOwnerDAO;
import com.revolut.account.domain.Account;
import com.revolut.account.domain.AccountOperation;
import com.revolut.account.domain.AccountOwner;
import com.revolut.account.domain.Address;
import com.revolut.account.domain.Amount;
import com.revolut.account.service.AccountService;
import com.revolut.dao.DatabaseTest;
import com.revolut.dao.TransactionManager;

public class AccountServiceTest extends DatabaseTest {
	@Inject
	private AccountService service;
	@Inject
	private TransactionManager transactionManager;

	@Test
	public void testCreateAccount() throws SQLException, ServiceException {
		AccountOwner ao = new AccountOwner(-1, "Victor", new Address("Saburovo park"), "verevic@revolut.com");
		AccountOwner owner = transactionManager.runWithResult(c -> AccountOwnerDAO.createOwner(c, ao));
		// New account
		BigDecimal amount = new BigDecimal("3.5E+5");
		Currency ccy = Currency.getInstance("RUB");
		Account account = service.createAccount(owner.getId(), new Amount(amount, ccy));
		Assertions.assertNotNull(account);
		Assertions.assertEquals(owner.getId(), account.getOwnerId());
		Assertions.assertEquals(0, amount.compareTo(account.getAmount().getAmount()));
		Assertions.assertEquals(ccy, account.getAmount().getCurrency());
		// AccountOperation
		List<AccountOperation> operations = transactionManager.runWithResult(c -> AccountOperationDAO.getOperationsFor(c, account));
		Assertions.assertNotNull(operations);
		Assertions.assertEquals(1, operations.size());
		AccountOperation op = operations.get(0);
		Assertions.assertTrue(op.getId() >= 0);
		Assertions.assertEquals(account.getId(), op.getAccountId());
		Assertions.assertNotNull(op.getTimestamp());
		Assertions.assertEquals("Account created", op.getDetails());

		Assertions.assertEquals(0, amount.compareTo(op.getBalance().getAmount()));
		Assertions.assertEquals(ccy, op.getBalance().getCurrency());
	}

	@Test
	public void testGetAccountsFor() throws SQLException, ServiceException {
		AccountOwner ao = new AccountOwner(-1, "Victor", new Address("Saburovo park"), "verevic@revolut.com");
		AccountOwner owner = transactionManager.runWithResult(c -> AccountOwnerDAO.createOwner(c, ao));

		List<Account> accounts = service.getAccountsFor(owner.getId());
		Assertions.assertNotNull(accounts);
		Assertions.assertEquals(0, accounts.size());

		BigDecimal amount = new BigDecimal("3.5E+5");
		Currency ccy = Currency.getInstance("RUB");
		Account account = service.createAccount(owner.getId(), new Amount(amount, ccy));
		
		accounts = service.getAccountsFor(owner.getId());
		Assertions.assertNotNull(accounts);
		Assertions.assertEquals(1, accounts.size());
		Assertions.assertEquals(account, accounts.get(0));
	}

	@Test
	public void testDeposit() throws SQLException, ServiceException {
		AccountOwner ao = new AccountOwner(-1, "Victor", new Address("Saburovo park"), "verevic@revolut.com");
		AccountOwner owner = transactionManager.runWithResult(c -> AccountOwnerDAO.createOwner(c, ao));

		BigDecimal amount = new BigDecimal("3.5E+5");
		Currency ccy = Currency.getInstance("RUB");
		Account account = service.createAccount(owner.getId(), new Amount(amount, ccy));
	
		BigDecimal credit = new BigDecimal(123456);
		Amount creditAmount = new Amount(credit, ccy);
		Account acc = service.deposit(account.getId(), creditAmount);
		Assertions.assertNotNull(acc);
		Assertions.assertEquals(account.getId(), acc.getId());
		BigDecimal expected = amount.add(credit);
		Assertions.assertEquals(0, expected.compareTo(acc.getAmount().getAmount()));
		// Operations
		List<AccountOperation> operations = transactionManager.runWithResult(c -> AccountOperationDAO.getOperationsFor(c, account));
		Assertions.assertNotNull(operations);
		Assertions.assertEquals(2, operations.size());

		AccountOperation op = operations.get(1);
		Assertions.assertEquals(account.getId(), op.getAccountId());
		Assertions.assertNotNull(op.getTimestamp());
		Assertions.assertEquals(String.format("A deposit for %s", creditAmount), op.getDetails());

		Assertions.assertEquals(0, expected.compareTo(op.getBalance().getAmount()));
		Assertions.assertEquals(ccy, op.getBalance().getCurrency());
	}

	@Test
	public void testWithdraw() throws SQLException, ServiceException {
		AccountOwner ao = new AccountOwner(-1, "Victor", new Address("Saburovo park"), "verevic@revolut.com");
		AccountOwner owner = transactionManager.runWithResult(c -> AccountOwnerDAO.createOwner(c, ao));

		BigDecimal amount = new BigDecimal("3.5E+5");
		Currency ccy = Currency.getInstance("RUB");
		Account account = service.createAccount(owner.getId(), new Amount(amount, ccy));

		BigDecimal debit = new BigDecimal(123456);
		Amount debitAmount = new Amount(debit, ccy);
		Account acc = service.withdraw(account.getId(), debitAmount);
		Assertions.assertNotNull(acc);
		Assertions.assertEquals(account.getId(), acc.getId());
		BigDecimal expected = amount.subtract(debit);
		Assertions.assertEquals(0, expected.compareTo(acc.getAmount().getAmount()));
		// Operations
		List<AccountOperation> operations = transactionManager.runWithResult(c -> AccountOperationDAO.getOperationsFor(c, account));
		Assertions.assertNotNull(operations);
		Assertions.assertEquals(2, operations.size());

		AccountOperation op = operations.get(1);
		Assertions.assertEquals(account.getId(), op.getAccountId());
		Assertions.assertNotNull(op.getTimestamp());
		Assertions.assertEquals(String.format("A withdrawal of %s", debitAmount), op.getDetails());

		Assertions.assertEquals(0, expected.compareTo(op.getBalance().getAmount()));
		Assertions.assertEquals(ccy, op.getBalance().getCurrency());
	}

	@Test
	public void testTransfer() throws SQLException, ServiceException {
		AccountOwner ao = new AccountOwner(-1, "Victor", new Address("Saburovo park"), "verevic@revolut.com");
		AccountOwner owner = transactionManager.runWithResult(c -> AccountOwnerDAO.createOwner(c, ao));

		BigDecimal amount = new BigDecimal("3.5E+5");
		Currency ccy = Currency.getInstance("RUB");
		Account from = service.createAccount(owner.getId(), new Amount(amount, ccy));
		Account to = service.createAccount(owner.getId(), new Amount(BigDecimal.ZERO, ccy));

		Amount transferAmount = new Amount(amount, ccy);
		service.transfer(from.getId(), to.getId(), transferAmount);
		List<Account> accounts = service.getAccountsFor(owner.getId());
		Assertions.assertEquals(2, accounts.size());

		// From
		Account newFrom = accounts.get(0);
		Assertions.assertEquals(from.getId(), newFrom.getId());
		Assertions.assertEquals(0, BigDecimal.ZERO.compareTo(newFrom.getAmount().getAmount()));
		Assertions.assertEquals(ccy, newFrom.getAmount().getCurrency());
		// Operations
		List<AccountOperation> operations = transactionManager.runWithResult(c -> AccountOperationDAO.getOperationsFor(c, newFrom));
		Assertions.assertNotNull(operations);
		Assertions.assertEquals(2, operations.size());
		AccountOperation op = operations.get(1);
		Assertions.assertEquals(newFrom.getId(), op.getAccountId());
		Assertions.assertNotNull(op.getTimestamp());
		Assertions.assertEquals(String.format("A transfer of %s to accountId:%d", transferAmount, to.getId()), op.getDetails());

		Assertions.assertEquals(0, BigDecimal.ZERO.compareTo(op.getBalance().getAmount()));
		Assertions.assertEquals(ccy, op.getBalance().getCurrency());

		// To
		Account newTo = accounts.get(1);
		Assertions.assertEquals(to.getId(), newTo.getId());
		Assertions.assertEquals(0, amount.compareTo(newTo.getAmount().getAmount()));
		Assertions.assertEquals(ccy, newTo.getAmount().getCurrency());
		// Operations
		operations = transactionManager.runWithResult(c -> AccountOperationDAO.getOperationsFor(c, to));
		Assertions.assertNotNull(operations);
		Assertions.assertEquals(2, operations.size());

		op = operations.get(1);
		Assertions.assertEquals(newTo.getId(), op.getAccountId());
		Assertions.assertNotNull(op.getTimestamp());
		Assertions.assertEquals(String.format("A transfer of %s from accountId:%d", transferAmount, from.getId()), op.getDetails());

		Assertions.assertEquals(0, amount.compareTo(op.getBalance().getAmount()));
		Assertions.assertEquals(ccy, op.getBalance().getCurrency());
	}
}
