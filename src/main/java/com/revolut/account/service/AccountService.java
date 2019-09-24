package com.revolut.account.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;

import com.revolut.ServiceException;
import com.revolut.account.dao.AccountDAO;
import com.revolut.account.dao.AccountOperationDAO;
import com.revolut.account.domain.Account;
import com.revolut.account.domain.AccountOperation;
import com.revolut.account.domain.AccountOwner;
import com.revolut.account.domain.Amount;
import com.revolut.dao.TransactionManager;

public class AccountService {
	private final TransactionManager transactionManager;

	@Inject
	public AccountService(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	// Have to either specify currency or amount. Let it be amount (it could be empty, anyway)
	public Account createAccount(AccountOwner owner, Amount amount) throws ServiceException {
		try {
			return transactionManager.runWithResult(c -> {
				Account account = AccountDAO.createAccount(c, owner, amount);
				// TBD: should I enable start==null?
				AccountOperation operation = new AccountOperation(-1, account.getId(), null, "Account created",
						new Amount(BigDecimal.ZERO, amount.getCurrency()), amount);
				AccountOperationDAO.createOperation(c, operation);
				return account;

			});
		} catch (SQLException e) {
			throw new ServiceException(String.format("Failed to create an account for %s", owner), e);
		}
	}

	public List<Account> getAccountsFor(AccountOwner owner) throws ServiceException {
		try {
			return transactionManager.runWithResult(c -> AccountDAO.getAccounts(c, owner));
		} catch (SQLException e) {
			throw new ServiceException(String.format("Failed to retrieve accounts for %s", owner), e);
		}
	}

	public Account deposit(Account account, Amount amount) throws ServiceException {
		try {
			return transactionManager.runWithResult(c -> {
				Account changed = AccountDAO.credit(c, account, amount);
				AccountOperation operation = new AccountOperation(-1, account.getId(), null,
						String.format("A deposit for %s", amount), account.getAmount(), changed.getAmount());
				AccountOperationDAO.createOperation(c, operation);
				return changed;
			});
		} catch (SQLException e) {
			throw new ServiceException(String.format("Failed to deposit %s to %s", amount, account), e);
		}
		
	}

	// I'd prefer to have to different operations instead of introducing a negative amount...
	public Account withdraw(Account account, Amount amount) throws ServiceException {
		try {
			return transactionManager.runWithResult(c -> {
				Account changed = AccountDAO.debit(c, account, amount);
				AccountOperation operation = new AccountOperation(-1, account.getId(), null,
						String.format("A withdrawal of %s", amount), account.getAmount(), changed.getAmount());
				AccountOperationDAO.createOperation(c, operation);
				return changed;
			});
		} catch (SQLException e) {
			throw new ServiceException(String.format("Failed to withdraw %s from %s", amount, account), e);
		}
	}

	// not sure if should return updated accounts here...
	public void transfer(Account from, Account to, Amount amount) throws ServiceException {
		try {
			transactionManager.run(c -> {
				Account newFrom = AccountDAO.debit(c, from, amount);
				AccountOperation operation = new AccountOperation(-1, from.getId(), null,
						String.format("A transfer of %s to %s", amount, to), from.getAmount(), newFrom.getAmount());
				AccountOperationDAO.createOperation(c, operation);

				Account newTo = AccountDAO.credit(c, to, amount);
				operation = new AccountOperation(-1, to.getId(), null,
						String.format("A transfer of %s from %s", amount, from), to.getAmount(), newTo.getAmount());
				AccountOperationDAO.createOperation(c, operation);
			});
		} catch (SQLException e) {
			throw new ServiceException(String.format("Transfer from account id:%d to account id:%d failed", from.getId(), to.getId()), e);
		}
	}
}
