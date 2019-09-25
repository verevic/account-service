package com.revolut.account.service;

import java.sql.SQLException;
import java.util.Currency;
import java.util.List;

import javax.inject.Inject;

import com.revolut.ServiceException;
import com.revolut.account.dao.AccountDAO;
import com.revolut.account.dao.AccountOperationDAO;
import com.revolut.account.domain.Account;
import com.revolut.account.domain.AccountOperation;
import com.revolut.account.domain.Amount;
import com.revolut.dao.TransactionManager;

public class AccountService {
	private final TransactionManager transactionManager;

	@Inject
	public AccountService(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	// Have to either specify currency or amount. Let it be amount (it could be empty, anyway)
	public Account createAccount(long ownerId, Amount amount) throws ServiceException {
		try {
			return transactionManager.runWithResult(c -> {
				Account account = AccountDAO.createAccount(c, ownerId, amount);
				AccountOperation operation = new AccountOperation(-1, account.getId(), null, "Account created", amount);
				AccountOperationDAO.createOperation(c, operation);
				return account;

			});
		} catch (SQLException e) {
			throw new ServiceException(String.format("Failed to create an account for %d", ownerId), e);
		}
	}

	public List<Account> getAccountsFor(long ownerId) throws ServiceException {
		try {
			return transactionManager.runWithResult(c -> AccountDAO.getAccounts(c, ownerId));
		} catch (SQLException e) {
			throw new ServiceException(String.format("Failed to retrieve accounts for %d", ownerId), e);
		}
	}

	private void carrenciesMatch(Account account, Amount amount) throws SQLException {
		Currency inputCcy = amount.getCurrency();
		Currency accountCcy = account.getAmount().getCurrency();
		if (!accountCcy.equals(inputCcy)) {
			throw new SQLException(String.format("Account(%d) currency(%s) doesn't match operation currency(%s)",
					account.getId(), accountCcy.getCurrencyCode(), inputCcy.getCurrencyCode()));
		}
	}

	public Account deposit(long accountId, Amount amount) throws ServiceException {
		try {
			return transactionManager.runWithResult(c -> {
				Account changed = AccountDAO.credit(c, accountId, amount);
				carrenciesMatch(changed, amount);
				AccountOperation operation = new AccountOperation(-1, accountId, null,
						String.format("A deposit for %s", amount), changed.getAmount());
				AccountOperationDAO.createOperation(c, operation);
				return changed;
			});
		} catch (SQLException e) {
			throw new ServiceException(String.format("Failed to deposit %s to accountId:%d", amount, accountId), e);
		}
		
	}

	// I'd prefer to have to different operations instead of introducing a negative amount...
	public Account withdraw(long accountId, Amount amount) throws ServiceException {
		try {
			return transactionManager.runWithResult(c -> {
				Account changed = AccountDAO.debit(c, accountId, amount);
				carrenciesMatch(changed, amount);
				AccountOperation operation = new AccountOperation(-1, accountId, null,
						String.format("A withdrawal of %s", amount), changed.getAmount());
				AccountOperationDAO.createOperation(c, operation);
				return changed;
			});
		} catch (SQLException e) {
			throw new ServiceException(String.format("Failed to withdraw %s from accountId:%d", amount, accountId), e);
		}
	}

	// not sure if should return updated accounts here...
	public void transfer(long fromId, long toId, Amount amount) throws ServiceException {
		try {
			transactionManager.run(c -> {
				Account newFrom = AccountDAO.debit(c, fromId, amount);
				carrenciesMatch(newFrom, amount);
				AccountOperation operation = new AccountOperation(-1, fromId, null,
						String.format("A transfer of %s to accountId:%d", amount, toId), newFrom.getAmount());
				AccountOperationDAO.createOperation(c, operation);

				Account newTo = AccountDAO.credit(c, toId, amount);
				carrenciesMatch(newTo, amount);
				operation = new AccountOperation(-1, toId, null,
						String.format("A transfer of %s from accountId:%d", amount, fromId), newTo.getAmount());
				AccountOperationDAO.createOperation(c, operation);
			});
		} catch (SQLException e) {
			throw new ServiceException(String.format("Transfer from account id:%d to account id:%d failed", fromId, toId), e);
		}
	}

	public List<AccountOperation> getOperationsFor(long accountId) throws ServiceException {
		try {
			return transactionManager.runWithResult(c -> AccountOperationDAO.getOperationsFor(c, accountId));
		} catch (SQLException e) {
			throw new ServiceException(String.format("Failed to get operations for accountId:%d", accountId), e);
		}
	}
}
