package com.revolut.account.service;

import java.util.List;

import com.revolut.ServiceException;
import com.revolut.account.domain.Account;
import com.revolut.account.domain.AccountOwner;
import com.revolut.account.domain.Amount;

public interface AccountService {
	// Have to either specify currency or amount. Let it be amount (it could be empty, anyway)
	Account createAccout(AccountOwner owner, Amount amount) throws ServiceException;
	List<Account> getAccountsFor(AccountOwner owner) throws ServiceException;

	Account deposit(Account account, Amount amount) throws ServiceException;
	// I'd prefer to have to different operations instead of introducing a negative amount...
	Account withdraw(Account account, Amount amount) throws ServiceException;
	// not sure if should return updated accounts here...
	void transfer(Account from, Account to, Amount amount) throws ServiceException;
}
