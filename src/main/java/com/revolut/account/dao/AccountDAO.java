package com.revolut.account.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Currency;
import java.util.List;

import com.revolut.account.domain.Account;
import com.revolut.account.domain.AccountOwner;
import com.revolut.account.domain.Amount;
import com.revolut.dao.JDBCUtils;

public class AccountDAO {
	private static final String CREATE_ACCOUNT =
			"insert into Account (owner_id, amount, ccy) values (%d, %s, '%s')";
	public static Account createAccount(Connection c, AccountOwner owner, Amount amount) throws SQLException {
		String sql = String.format(CREATE_ACCOUNT, owner.getId(), amount.getAmount().toString(), amount.getCurrency().getCurrencyCode());
		return JDBCUtils.executeInsert(c, sql, AccountDAO::fromResultSet, 4);
		
	}

	private static final String GET_ACCOUNTS =
			"select id, owner_id, amount, ccy from Account where owner_id = %d order by id";
	public static List<Account> getAccounts(Connection c, AccountOwner owner) throws SQLException {
		String sql = String.format(GET_ACCOUNTS, owner.getId());
		return JDBCUtils.executeSelect(c, sql, AccountDAO::fromResultSet);
	}

	private static final String CREDIT =
			"update Account set amount = amount+%s where id=%d and ccy='%s';" +
			"select id, owner_id, amount, ccy from Account where id=%d;";
	public static Account credit(Connection c, Account account, Amount amount) throws SQLException {
		String sql = String.format(CREDIT, amount.getAmount(), account.getId(), account.getAmount().getCurrency().getCurrencyCode(), account.getId());
		return JDBCUtils.executeSelectSingle(c, sql, AccountDAO::fromResultSet);
	}

	private static final String DEBIT =
			"update Account set amount = amount-%s where id=%d and ccy='%s';" +
			"select id, owner_id, amount, ccy from Account where id=%d;";
	public static Account debit(Connection c, Account account, Amount amount) throws SQLException {
		// TODO: make sure cannot withdraw more than have
		String sql = String.format(DEBIT, amount.getAmount(), account.getId(), account.getAmount().getCurrency().getCurrencyCode(), account.getId());
		return JDBCUtils.executeSelectSingle(c, sql, AccountDAO::fromResultSet);
	}

	static Account fromResultSet(ResultSet rs) throws SQLException {
		long id = rs.getLong("id");
		long ownerId = rs.getLong("owner_id");
		BigDecimal amount = rs.getBigDecimal("amount");
		Currency ccy = Currency.getInstance(rs.getString("ccy"));
		return new Account(id, ownerId, new Amount(amount, ccy));
	}
}
