package com.revolut.account;

import com.revolut.BeanFactory;
import com.revolut.account.dao.AccountDAO;
import com.revolut.account.dao.AccountOperationDAO;
import com.revolut.account.dao.AccountOwnerDAO;
import com.revolut.account.service.AccountOwnerService;
import com.revolut.account.service.AccountService;

public interface AccountBeanFactory extends BeanFactory {
	AccountService getAccountService();
	AccountOwnerService getAccountOwnerService();

	AccountDAO getAccountDAO();
	AccountOwnerDAO getAccountOwnerDAO();
	AccountOperationDAO getAccountOperationDAO();
}
