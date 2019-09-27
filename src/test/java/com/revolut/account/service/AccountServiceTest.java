package com.revolut.account.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Currency;
import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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
import com.revolut.exception.BusinessRuleException;
import com.revolut.exception.ServiceException;

public class AccountServiceTest extends DatabaseTest {
	@Inject
	private AccountService service;
	@Inject
	private TransactionManager transactionManager;

	@Test
	public void testCreateAccount() throws SQLException, BusinessRuleException, ServiceException {
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
		List<AccountOperation> operations = transactionManager.runWithResult(c -> AccountOperationDAO.getOperationsFor(c, account.getId()));
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
	public void testGetAccountsFor() throws SQLException, BusinessRuleException, ServiceException {
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
	public void testDeposit() throws SQLException, BusinessRuleException, ServiceException {
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
		List<AccountOperation> operations = transactionManager.runWithResult(c -> AccountOperationDAO.getOperationsFor(c, account.getId()));
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
	public void testWithdraw() throws SQLException, BusinessRuleException, ServiceException {
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
		List<AccountOperation> operations = transactionManager.runWithResult(c -> AccountOperationDAO.getOperationsFor(c, account.getId()));
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
	public void testTransfer() throws SQLException, BusinessRuleException, ServiceException {
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
		List<AccountOperation> operations = transactionManager.runWithResult(c -> AccountOperationDAO.getOperationsFor(c, from.getId()));
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
		operations = transactionManager.runWithResult(c -> AccountOperationDAO.getOperationsFor(c, to.getId()));
		Assertions.assertNotNull(operations);
		Assertions.assertEquals(2, operations.size());

		op = operations.get(1);
		Assertions.assertEquals(newTo.getId(), op.getAccountId());
		Assertions.assertNotNull(op.getTimestamp());
		Assertions.assertEquals(String.format("A transfer of %s from accountId:%d", transferAmount, from.getId()), op.getDetails());

		Assertions.assertEquals(0, amount.compareTo(op.getBalance().getAmount()));
		Assertions.assertEquals(ccy, op.getBalance().getCurrency());
	}

	@Test
	public void testDepositWithDifferentCcy() throws SQLException, BusinessRuleException, ServiceException {
		AccountOwner ao = new AccountOwner(-1, "Victor", new Address("Saburovo park"), "verevic@revolut.com");
		AccountOwner owner = transactionManager.runWithResult(c -> AccountOwnerDAO.createOwner(c, ao));

		BigDecimal initial = new BigDecimal("3.5E+5");
		Currency initialCcy = Currency.getInstance("RUB");
		Account account = service.createAccount(owner.getId(), new Amount(initial, initialCcy));
	
		BigDecimal credit = new BigDecimal(123456);
		Currency creditCcy = Currency.getInstance("EUR");
		Amount creditAmount = new Amount(credit, creditCcy);

		try {
			service.deposit(account.getId(), creditAmount);
			Assertions.fail("The call is expected to fail");
		} catch (ServiceException e) {
			Assertions.assertEquals(String.format("Failed to deposit %s to accountId:%d", creditAmount, account.getId()), e.getMessage());
			Throwable t = e.getCause();
			Assertions.assertNotNull(t);
			Assertions.assertEquals(String.format("Account(%d) currency(%s) doesn't match operation currency(%s)",
					account.getId(), initialCcy.getCurrencyCode(), creditCcy.getCurrencyCode()), t.getMessage());
		}
		// balance
		List<Account> accounts = service.getAccountsFor(owner.getId());
		Assertions.assertEquals(1, accounts.size());
		account = accounts.get(0);
		Assertions.assertEquals(0, initial.compareTo(account.getAmount().getAmount()));
		Assertions.assertEquals(initialCcy, account.getAmount().getCurrency());
		// operations
		List<AccountOperation> operations = service.getOperationsFor(account.getId());
		Assertions.assertEquals(1, operations.size());
	}

	@Test
	public void testWithdrawWithDifferentCcy() throws SQLException, BusinessRuleException, ServiceException {
		AccountOwner ao = new AccountOwner(-1, "Victor", new Address("Saburovo park"), "verevic@revolut.com");
		AccountOwner owner = transactionManager.runWithResult(c -> AccountOwnerDAO.createOwner(c, ao));

		BigDecimal initial = new BigDecimal("3.5E+5");
		Currency initialCcy = Currency.getInstance("RUB");
		Account account = service.createAccount(owner.getId(), new Amount(initial, initialCcy));
	
		BigDecimal debit = new BigDecimal(123456);
		Currency debitCcy = Currency.getInstance("EUR");
		Amount debitAmount = new Amount(debit, debitCcy);

		try {
			service.withdraw(account.getId(), debitAmount);
			Assertions.fail("The call is expected to fail");
		} catch (ServiceException e) {
			Assertions.assertEquals(String.format("Failed to withdraw %s from accountId:%d", debitAmount, account.getId()), e.getMessage());
			Throwable t = e.getCause();
			Assertions.assertNotNull(t);
			Assertions.assertEquals(String.format("Account(%d) currency(%s) doesn't match operation currency(%s)",
					account.getId(), initialCcy.getCurrencyCode(), debitCcy.getCurrencyCode()), t.getMessage());
		}
		// balance
		List<Account> accounts = service.getAccountsFor(owner.getId());
		Assertions.assertEquals(1, accounts.size());
		account = accounts.get(0);
		Assertions.assertEquals(0, initial.compareTo(account.getAmount().getAmount()));
		Assertions.assertEquals(initialCcy, account.getAmount().getCurrency());
		// operations
		List<AccountOperation> operations = service.getOperationsFor(account.getId());
		Assertions.assertEquals(1, operations.size());
	}

	@Test
	public void testTransferWithDifferentCcy() throws SQLException, BusinessRuleException, ServiceException {
		AccountOwner ao = new AccountOwner(-1, "Victor", new Address("Saburovo park"), "verevic@revolut.com");
		AccountOwner owner = transactionManager.runWithResult(c -> AccountOwnerDAO.createOwner(c, ao));

		BigDecimal initial = new BigDecimal("3.5E+5");
		Currency rub = Currency.getInstance("RUB");
		Currency eur = Currency.getInstance("EUR");

		Account fromRub = service.createAccount(owner.getId(), new Amount(initial, rub));
		Account toEur = service.createAccount(owner.getId(), new Amount(BigDecimal.ZERO, eur));

		BigDecimal amount = new BigDecimal(123456);
		Amount rubAmount = new Amount(amount, rub);
		Amount eurAmount = new Amount(amount, eur);

		// from fails
		try {
			service.transfer(fromRub.getId(), toEur.getId(), eurAmount);
			Assertions.fail("The call is expected to fail");
		} catch (ServiceException e) {
			Assertions.assertEquals(String.format("Transfer from account id:%d to account id:%d failed",
					fromRub.getId(), toEur.getId()), e.getMessage());
			Throwable t = e.getCause();
			Assertions.assertNotNull(t);
			Assertions.assertEquals(String.format("Account(%d) currency(%s) doesn't match operation currency(%s)",
					fromRub.getId(), rub.getCurrencyCode(), eur.getCurrencyCode()), t.getMessage());
		}
		// to fails
		try {
			service.transfer(fromRub.getId(), toEur.getId(), rubAmount);
			Assertions.fail("The call is expected to fail");
		} catch (ServiceException e) {
			Assertions.assertEquals(String.format("Transfer from account id:%d to account id:%d failed",
					fromRub.getId(), toEur.getId()), e.getMessage());
			Throwable t = e.getCause();
			Assertions.assertNotNull(t);
			Assertions.assertEquals(String.format("Account(%d) currency(%s) doesn't match operation currency(%s)",
					toEur.getId(), eur.getCurrencyCode(), rub.getCurrencyCode()), t.getMessage());
		}
		// FROM
		// balance
		List<Account> accounts = service.getAccountsFor(owner.getId());
		Assertions.assertEquals(2, accounts.size());
		Account account = accounts.get(0);
		Assertions.assertEquals(fromRub.getId(), account.getId());
		Assertions.assertEquals(0, initial.compareTo(account.getAmount().getAmount()));
		Assertions.assertEquals(rub, account.getAmount().getCurrency());
		// operations
		List<AccountOperation> operations = service.getOperationsFor(account.getId());
		Assertions.assertEquals(1, operations.size());
		// TO
		// balance
		account = accounts.get(1);
		Assertions.assertEquals(toEur.getId(), account.getId());
		Assertions.assertEquals(0, BigDecimal.ZERO.compareTo(account.getAmount().getAmount()));
		Assertions.assertEquals(eur, account.getAmount().getCurrency());
		// operations
		operations = service.getOperationsFor(account.getId());
		Assertions.assertEquals(1, operations.size());
	}
}
