package com.revolut.account.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.revolut.account.domain.AccountOwner;

public interface AccountOwnerDAO {
	AccountOwner createOwner(Connection c, AccountOwner owner) throws SQLException;
	List<AccountOwner> getOwners(Connection c) throws SQLException;
}
