package com.revolut.account.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.revolut.ServiceException;
import com.revolut.account.dao.AccountOwnerDAO;
import com.revolut.account.domain.AccountOwner;
import com.revolut.account.domain.Address;
import com.revolut.account.service.AccountOwnerService;
import com.revolut.db.ConnectionFactory;

class AccountOwnerServiceImpl implements AccountOwnerService {
	private final ConnectionFactory connectionFactory;
	private final AccountOwnerDAO dao;

	public AccountOwnerServiceImpl(ConnectionFactory connectionFactory, AccountOwnerDAO dao) {
		this.connectionFactory = connectionFactory;
		this.dao = dao;
	}

	@Override
	public AccountOwner createAccoutOwner(String name, String address, String email) throws ServiceException {
		try (Connection c = connectionFactory.getConnection()) {
			return dao.createOwner(c, new AccountOwner(-1, name, new Address(address), email));
		} catch (SQLException e) {
			throw new ServiceException(String.format("Failed to create account owner %s", name), e);
		}
	}

	@Override
	public List<AccountOwner> getOwners() throws ServiceException {
		try (Connection c = connectionFactory.getConnection()) {
			return dao.getOwners(c);
		} catch (SQLException e) {
			throw new ServiceException(String.format("Failed to retrieve account owners"), e);
		}
	}
}
