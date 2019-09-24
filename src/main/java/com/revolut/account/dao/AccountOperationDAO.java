package com.revolut.account.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Currency;
import java.util.Date;
import java.util.List;

import com.revolut.account.domain.Account;
import com.revolut.account.domain.AccountOperation;
import com.revolut.account.domain.Amount;
import com.revolut.dao.JDBCUtils;

public class AccountOperationDAO {
	private static final String CREATE_OPERATION =
			"insert into AccountOperation (account_id, details, start, end, ccy) values (%d, %s, %s, %s, '%s')";
	public static AccountOperation createOperation(Connection c, AccountOperation op) throws SQLException {
		String sql = String.format(CREATE_OPERATION, op.getAccountId(), JDBCUtils.stringParam(op.getDetails()),
				op.getStartBalance().getAmount(), op.getEndBalance().getAmount(), op.getStartBalance().getCurrency());
		return JDBCUtils.executeInsert(c, sql, AccountOperationDAO::fromResultSet, 7);
	}

	private static final String LIST =
			"select id, account_id, created, details, start, end, ccy from AccountOperation where account_id = %d order by id";
	public static List<AccountOperation> getOperationsFor(Connection c, Account account) throws SQLException {
		String sql = String.format(LIST, account.getId());
		return JDBCUtils.executeSelect(c, sql, AccountOperationDAO::fromResultSet);
	}

	private static AccountOperation fromResultSet(ResultSet rs) throws SQLException {
		long id = rs.getLong("id");
		long accId = rs.getLong("account_id");
		Date timestamp = rs.getDate("created");
		String details = rs.getString("details");
		BigDecimal start = rs.getBigDecimal("start");
		BigDecimal end = rs.getBigDecimal("end");
		Currency ccy = Currency.getInstance(rs.getString("ccy"));

		return new AccountOperation(id, accId, timestamp, details, new Amount(start, ccy), new Amount(end, ccy));
	}
}
