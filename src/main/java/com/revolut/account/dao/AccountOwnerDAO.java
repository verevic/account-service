package com.revolut.account.dao;

import static com.revolut.dao.JDBCUtils.stringParam;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.revolut.account.domain.AccountOwner;
import com.revolut.account.domain.Address;
import com.revolut.dao.JDBCUtils;

public class AccountOwnerDAO {
	private static final String CREATE_OWNER = "insert into AccountOwner (name, address, email) values('%s', %s, %s)";
	public static AccountOwner createOwner(Connection c, AccountOwner owner) throws SQLException {
		String address = Optional.ofNullable(owner.getAddress())
				.map(Address::getAddress)
				.orElse(null);
		String sql = String.format(CREATE_OWNER,
				owner.getName(), stringParam(address), stringParam(owner.getEmail()));
		return JDBCUtils.executeInsert(c, sql, AccountOwnerDAO::fromResultSet, 4);
	}

	private static final String GET_OWNERS = "select id, name, address, email from AccountOwner";
	public static List<AccountOwner> list(Connection c) throws SQLException {
		return JDBCUtils.executeSelect(c, GET_OWNERS, AccountOwnerDAO::fromResultSet);
	}

	static AccountOwner fromResultSet(ResultSet rs) throws SQLException {
		long id = rs.getLong("id");
		String name = rs.getString("name");
		String address = rs.getString("address");
		String email = rs.getString("email");

		return new AccountOwner(id, name, address == null ? null : new Address(address), email);
	}
}
