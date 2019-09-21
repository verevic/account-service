package com.revolut.account.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.revolut.account.domain.Account;
import com.revolut.account.domain.AccountOperation;

public interface AccountOperationDAO {
	AccountOperation createOperation(Connection c, AccountOperation op) throws SQLException;
	List<AccountOperation> getOperationsFor(Connection c, Account account) throws SQLException;
}
