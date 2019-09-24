package com.revolut.account.dao;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Currency;
import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.revolut.account.domain.Account;
import com.revolut.account.domain.AccountOwner;
import com.revolut.account.domain.Address;
import com.revolut.account.domain.Amount;
import com.revolut.dao.DatabaseTest;
import com.revolut.dao.TransactionManager;

public class AccountDAOTest extends DatabaseTest {
	@Inject
	private TransactionManager transactionManager;

	@Test
	public void testCreateAccount() throws SQLException {
		AccountOwner owner = transactionManager.runWithResult(c -> AccountOwnerDAO.createOwner(c,
				new AccountOwner(-1, "Victor", new Address("Saburovo park"), "verevic@revolut.com")));

		BigDecimal amount = new BigDecimal("3.5E+5");
		Currency ccy = Currency.getInstance("RUB");
		Account account = transactionManager.runWithResult(c -> AccountDAO.createAccount(c, owner.getId(), new Amount(amount, ccy)));

		Assertions.assertNotNull(account);
		Assertions.assertTrue(account.getId() >= 0);
		Assertions.assertEquals(owner.getId(), account.getOwnerId());
		Assertions.assertEquals(amount, account.getAmount().getAmount().setScale(amount.scale()));
		Assertions.assertEquals(ccy, account.getAmount().getCurrency());
	}


	@Test
	public void testGetAccounts() throws SQLException {
		AccountOwner owner = transactionManager.runWithResult(c -> AccountOwnerDAO.createOwner(c,
				new AccountOwner(-1, "An owner", new Address("Address"), "email")));

		List<Account> accounts = transactionManager.runWithResult(c -> AccountDAO.getAccounts(c, owner.getId()));
		Assertions.assertNotNull(accounts);
		Assertions.assertEquals(0, accounts.size());

		BigDecimal amount = new BigDecimal("3.5E+5");
		Currency ccy = Currency.getInstance("RUB");
		Account account = transactionManager.runWithResult(c -> AccountDAO.createAccount(c, owner.getId(), new Amount(amount, ccy)));

		accounts = transactionManager.runWithResult(c -> AccountDAO.getAccounts(c, owner.getId()));
		Assertions.assertNotNull(accounts);
		Assertions.assertEquals(1, accounts.size());
		Assertions.assertEquals(account, accounts.get(0));
	}

	@Test
	public void testCredit() throws SQLException {
		AccountOwner owner = transactionManager.runWithResult(c -> AccountOwnerDAO.createOwner(c,
				new AccountOwner(-1, "Victor", new Address("Saburovo park"), "verevic@revolut.com")));

		BigDecimal amount = new BigDecimal("3.5E+5");
		BigDecimal credit = new BigDecimal("123456789");
		Currency ccy = Currency.getInstance("RUB");

		Account account = transactionManager.runWithResult(c -> AccountDAO.createAccount(c, owner.getId(), new Amount(amount, ccy)));
		Account credited = transactionManager.runWithResult(c -> AccountDAO.credit(c, account.getId(), new Amount(credit,ccy)));
		Assertions.assertNotNull(credited);
		Assertions.assertEquals(account.getId(), credited.getId());
		BigDecimal expected = amount.add(credit);
		Assertions.assertEquals(0, expected.compareTo(credited.getAmount().getAmount()));
		Assertions.assertEquals(ccy, credited.getAmount().getCurrency());
	}

	@Test
	public void testDebit() throws SQLException {
		AccountOwner owner = transactionManager.runWithResult(c -> AccountOwnerDAO.createOwner(c,
				new AccountOwner(-1, "Victor", new Address("Saburovo park"), "verevic@revolut.com")));

		BigDecimal amount = new BigDecimal("3.5E+5");
		BigDecimal debit = new BigDecimal("123456789");
		Currency ccy = Currency.getInstance("RUB");

		Account account = transactionManager.runWithResult(c -> AccountDAO.createAccount(c, owner.getId(), new Amount(amount, ccy)));

		Account debited = transactionManager.runWithResult(c -> AccountDAO.debit(c, account.getId(), new Amount(debit,ccy)));
		Assertions.assertNotNull(debited);
		Assertions.assertEquals(account.getId(), debited.getId());
		BigDecimal expected = amount.subtract(debit);
		Assertions.assertEquals(0, expected.compareTo(debited.getAmount().getAmount()));
		Assertions.assertEquals(ccy, debited.getAmount().getCurrency());
	}
}
