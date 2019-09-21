package com.revolut.account.impl;

import com.revolut.BeanFactory;
import com.revolut.account.AccountBeanFactory;
import com.revolut.account.dao.AccountDAO;
import com.revolut.account.dao.AccountOperationDAO;
import com.revolut.account.dao.AccountOwnerDAO;
import com.revolut.account.impl.jdbc.JDBCAccountDAO;
import com.revolut.account.impl.jdbc.JDBCAccountOperationDAO;
import com.revolut.account.impl.jdbc.JDBCAccountOwnerDAO;
import com.revolut.account.service.AccountOwnerService;
import com.revolut.account.service.AccountService;
import com.revolut.db.ConnectionFactory;

public class AccountBeanFactoryImpl implements AccountBeanFactory {
	private final BeanFactory beanFactory;// = new BeanFactoryImpl();

	public AccountBeanFactoryImpl(BeanFactory beanFactory) {
		this.beanFactory = beanFactory;
	}

	@Override
	public ConnectionFactory getConnectionFactory() {
		return beanFactory.getConnectionFactory();
	}

	@Override
	public AccountService getAccountService() {
		return new AccountServiceImpl(getConnectionFactory(), getAccountDAO(), getAccountOperationDAO());
	}

	@Override
	public AccountOwnerService getAccountOwnerService() {
		return new AccountOwnerServiceImpl(getConnectionFactory(), getAccountOwnerDAO());
	}

	@Override
	public AccountDAO getAccountDAO() {
		return new JDBCAccountDAO();
	}

	@Override
	public AccountOwnerDAO getAccountOwnerDAO() {
		return new JDBCAccountOwnerDAO();
	}

	@Override
	public AccountOperationDAO getAccountOperationDAO() {
		return new JDBCAccountOperationDAO();
	}
}
