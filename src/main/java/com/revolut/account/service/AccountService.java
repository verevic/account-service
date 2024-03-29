package com.revolut.account.service;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Currency;
import java.util.List;

import javax.inject.Inject;

import com.revolut.account.dao.AccountDAO;
import com.revolut.account.dao.AccountOperationDAO;
import com.revolut.account.domain.Account;
import com.revolut.account.domain.AccountOperation;
import com.revolut.account.domain.Amount;
import com.revolut.dao.TransactionManager;
import com.revolut.exception.BusinessRuleException;
import com.revolut.exception.ServiceException;

public class AccountService {
	private final TransactionManager transactionManager;

	@Inject
	public AccountService(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	private void carrenciesMatch(Account account, Amount amount) throws BusinessRuleException {
		Currency inputCcy = amount.getCurrency();
		Currency accountCcy = account.getAmount().getCurrency();
		if (!accountCcy.equals(inputCcy)) {
			throw new BusinessRuleException(String.format("Account(%d) currency(%s) doesn't match operation currency(%s)",
					account.getId(), accountCcy.getCurrencyCode(), inputCcy.getCurrencyCode()));
		}
	}

	private void checkPositiveBalance(Account account) throws BusinessRuleException {
		if (BigDecimal.ZERO.compareTo(account.getAmount().getAmount()) > 0) {
			// Might be slightly confusing with a new account, but will help with a transfer
			throw new BusinessRuleException(String.format("Account(%d) balance cannot be negative", account.getId()));
		}
	}

	// Have to either specify currency or amount. Let it be amount (it could be empty, anyway)
	public Account createAccount(long ownerId, Amount amount) throws ServiceException {
		try {
			return transactionManager.runWithResult(c -> {
				Account account = AccountDAO.createAccount(c, ownerId, amount);
				checkPositiveBalance(account);
				AccountOperation operation = new AccountOperation(-1, account.getId(), null, "Account created", amount);
				AccountOperationDAO.createOperation(c, operation);
				return account;

			});
		} catch (SQLException | BusinessRuleException e) {
			throw new ServiceException(String.format("Failed to create an account for %d", ownerId), e);
		}
	}

	public List<Account> getAccountsFor(long ownerId) throws ServiceException {
		try {
			return transactionManager.runWithResult(c -> AccountDAO.getAccounts(c, ownerId));
		} catch (SQLException | BusinessRuleException e) {
			throw new ServiceException(String.format("Failed to retrieve accounts for %d", ownerId), e);
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
		} catch (SQLException | BusinessRuleException e) {
			throw new ServiceException(String.format("Failed to deposit %s to accountId:%d", amount, accountId), e);
		}
		
	}

	// I'd prefer to have to different operations instead of introducing a negative amount...
	public Account withdraw(long accountId, Amount amount) throws ServiceException {
		try {
			return transactionManager.runWithResult(c -> {
				Account changed = AccountDAO.debit(c, accountId, amount);
				carrenciesMatch(changed, amount);
				checkPositiveBalance(changed);
				AccountOperation operation = new AccountOperation(-1, accountId, null,
						String.format("A withdrawal of %s", amount), changed.getAmount());
				AccountOperationDAO.createOperation(c, operation);
				return changed;
			});
		} catch (SQLException | BusinessRuleException e) {
			throw new ServiceException(String.format("Failed to withdraw %s from accountId:%d", amount, accountId), e);
		}
	}

	// not sure if should return updated accounts here...
	public void transfer(long fromId, long toId, Amount amount) throws ServiceException {
		try {
			transactionManager.run(c -> {
				Account newFrom = AccountDAO.debit(c, fromId, amount);
				checkPositiveBalance(newFrom);
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
		} catch (SQLException | BusinessRuleException e) {
			throw new ServiceException(String.format("Transfer from account id:%d to account id:%d failed", fromId, toId), e);
		}
	}

	public List<AccountOperation> getOperationsFor(long accountId) throws ServiceException {
		try {
			return transactionManager.runWithResult(c -> AccountOperationDAO.getOperationsFor(c, accountId));
		} catch (SQLException | BusinessRuleException e) {
			throw new ServiceException(String.format("Failed to get operations for accountId:%d", accountId), e);
		}
	}
}
