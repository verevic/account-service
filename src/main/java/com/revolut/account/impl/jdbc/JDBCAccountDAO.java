package com.revolut.account.impl.jdbc;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Currency;
import java.util.List;

import com.revolut.account.dao.AccountDAO;
import com.revolut.account.domain.Account;
import com.revolut.account.domain.AccountOwner;
import com.revolut.account.domain.Amount;
import com.revolut.db.JDBCUtils;

public class JDBCAccountDAO implements AccountDAO {
	private static final String CREATE_ACCOUNT =
		"insert into Account (owner_id, amount, ccy) values (%d, %s, '%s')";
	@Override
	public Account createAccount(Connection c, AccountOwner owner, Amount amount) throws SQLException {
		String sql = String.format(CREATE_ACCOUNT, owner.getId(), amount.getAmount().toString(), amount.getCurrency().getCurrencyCode());
		return JDBCUtils.executeInsert(c, sql, JDBCAccountDAO::fromResultSet, 4);
	}

	private static final String GET_ACCOUNTS =
			"select id, owner_id, amount, ccy from Account where owner_id = %d";
	@Override
	public List<Account> getAccounts(Connection c, AccountOwner owner) throws SQLException {
		long ownerId = owner.getId();
		String sql = String.format(GET_ACCOUNTS, ownerId);
		return JDBCUtils.executeSelect(c, sql, JDBCAccountDAO::fromResultSet);
	}

	// Note that we need isolation level READ_COMMITED or above...
	private static final String CREDIT =
		"update Account set amount = amount+%s where id=%d and ccy='%s';" +
		"select id, owner_id, amount, ccy from Account where id=%d;";
	@Override
	public Account credit(Connection c, Account account, Amount amount) throws SQLException {
		String sql = String.format(CREDIT, amount.getAmount(), account.getId(), account.getAmount().getCurrency().getCurrencyCode(), account.getId());
		return JDBCUtils.executeSelectSingle(c, sql, JDBCAccountDAO::fromResultSet);
	}

	private static final String DEBIT =
			"update Account set amount = amount-%s where id=%d and ccy='%s';" +
			"select id, owner_id, amount, ccy from Account where id=%d;";
	@Override
	public Account debit(Connection c, Account account, Amount amount) throws SQLException {
		// TODO: make sure cannot withdraw more than have
		String sql = String.format(DEBIT, amount.getAmount(), account.getId(), account.getAmount().getCurrency().getCurrencyCode(), account.getId());
		return JDBCUtils.executeSelectSingle(c, sql, JDBCAccountDAO::fromResultSet);
	}


	static Account fromResultSet(ResultSet rs) throws SQLException {
		long id = rs.getLong("id");
		long ownerId = rs.getLong("owner_id");
		BigDecimal amount = rs.getBigDecimal("amount");
		Currency ccy = Currency.getInstance(rs.getString("ccy"));
		return new Account(id, ownerId, new Amount(amount, ccy));
	}

//	public static void main(String[] args) {
//		BigDecimal n = new BigDecimal("0.000000001234567");
//		System.out.println(n.toString());
//	}
}
