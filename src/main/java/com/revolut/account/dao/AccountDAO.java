package com.revolut.account.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.revolut.account.domain.Account;
import com.revolut.account.domain.AccountOwner;
import com.revolut.account.domain.Amount;

public interface AccountDAO {
	Account createAccount(Connection c, AccountOwner owner, Amount amount) throws SQLException;
	List<Account> getAccounts(Connection c, AccountOwner owner) throws SQLException;

	Account credit(Connection c, Account account, Amount amount) throws SQLException;
	Account debit(Connection c, Account account, Amount amount) throws SQLException;
}
