package com.revolut.account.impl;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.revolut.ServiceException;
import com.revolut.account.dao.AccountDAO;
import com.revolut.account.dao.AccountOperationDAO;
import com.revolut.account.domain.Account;
import com.revolut.account.domain.AccountOperation;
import com.revolut.account.domain.AccountOwner;
import com.revolut.account.domain.Amount;
import com.revolut.account.service.AccountService;
import com.revolut.db.ConnectionFactory;
import com.revolut.db.JDBCUtils;

class AccountServiceImpl implements AccountService {
	private final ConnectionFactory connectionFactory;
	private final AccountDAO accountDAO;
	private final AccountOperationDAO operationDAO;

	public AccountServiceImpl(ConnectionFactory connectionFactory, AccountDAO accountDAO, AccountOperationDAO operationDAO) {
		this.connectionFactory = connectionFactory;
		this.accountDAO = accountDAO;
		this.operationDAO = operationDAO;
	}

	@Override
	public Account createAccout(AccountOwner owner, Amount amount) throws ServiceException {
		try {
			return JDBCUtils.singleTransaction(connectionFactory, c -> {
				Account account = accountDAO.createAccount(c, owner, amount);
				// TBD: should I enable start==null?
				AccountOperation operation = new AccountOperation(-1, account.getId(), null, "Account created",
						new Amount(BigDecimal.ZERO, amount.getCurrency()), amount);
				operationDAO.createOperation(c, operation);
				return account;
			});
		} catch (SQLException e) {
			throw new ServiceException(String.format("Failed to create an account for %s", owner), e);
		}
	}

	@Override
	public List<Account> getAccountsFor(AccountOwner owner) throws ServiceException {
		try (Connection c = connectionFactory.getConnection()) {
			return accountDAO.getAccounts(c, owner);
		} catch (SQLException e) {
			throw new ServiceException(String.format("Failed to retrieve accounts for %s", owner), e);
		}
	}

	@Override
	public Account deposit(Account account, Amount amount) throws ServiceException {
		try {
			return JDBCUtils.singleTransaction(connectionFactory, c-> {
				Account changed = accountDAO.credit(c, account, amount);
				AccountOperation operation = new AccountOperation(-1, account.getId(), null,
						String.format("A depoisit for %s", amount), account.getAmount(), changed.getAmount());
				operationDAO.createOperation(c, operation);
				return changed;
			});
		} catch (SQLException e) {
			throw new ServiceException(String.format("Failed to deposit %s to %s", amount, account), e);
		}
	}

	// I'd prefer to have two different operations instead of introducing a negative amount...
	@Override
	public Account withdraw(Account account, Amount amount) throws ServiceException {
		try {
			// TODO: there's no point in doing string formatting within the x-action
			return JDBCUtils.singleTransaction(connectionFactory, c-> {
				Account changed = accountDAO.debit(c, account, amount);
				AccountOperation operation = new AccountOperation(-1, account.getId(), null,
						String.format("A withdrawal of %s", amount), account.getAmount(), changed.getAmount());
				operationDAO.createOperation(c, operation);
				return changed;
			});
		} catch (SQLException e) {
			throw new ServiceException(String.format("Failed to withdraw %s from %s", amount, account), e);
		}
	}

	@Override
	public void transfer(Account from, Account to, Amount amount) throws ServiceException {
		try {
			JDBCUtils.singleTransaction(connectionFactory, c -> {
				Account newFrom = accountDAO.debit(c, from, amount);
				AccountOperation operation = new AccountOperation(-1, from.getId(), null,
						String.format("A transfer of %s to %s", amount, to), from.getAmount(), newFrom.getAmount());
				operationDAO.createOperation(c, operation);

				Account newTo = accountDAO.credit(c, to, amount);
				operation = new AccountOperation(-1, to.getId(), null,
						String.format("A transfer of %s from %s", amount, from), to.getAmount(), newTo.getAmount());
				operationDAO.createOperation(c, operation);
				return null;
			});
		} catch (SQLException e) {
			throw new ServiceException(String.format("Transfer from account id:%d to account id:%d failed", from.getId(), to.getId()), e);
		}
	}
}
