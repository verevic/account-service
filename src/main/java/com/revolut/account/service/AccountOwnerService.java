package com.revolut.account.service;

import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.revolut.account.dao.AccountOwnerDAO;
import com.revolut.account.domain.AccountOwner;
import com.revolut.account.domain.Address;
import com.revolut.dao.TransactionManager;
import com.revolut.exception.ServiceException;

@Singleton
public class AccountOwnerService {
	private final TransactionManager transactionManager;

	@Inject
	public AccountOwnerService(TransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

	public AccountOwner createOwner(String name, String address, String email) throws ServiceException {
		AccountOwner owner = new AccountOwner(-1, name, new Address(address), email);
		try {
			return transactionManager.runWithResult(c -> AccountOwnerDAO.createOwner(c, owner));
		} catch (SQLException e) {
			throw new ServiceException(String.format("Failed to create account owner %s", name), e);
		}
	}

	public List<AccountOwner> getOwners() throws ServiceException {
		try {
			return transactionManager.runWithResult(AccountOwnerDAO::list);
		} catch (SQLException e) {
			throw new ServiceException(String.format("Failed to retrieve account owners"), e);
		}
	}
}
