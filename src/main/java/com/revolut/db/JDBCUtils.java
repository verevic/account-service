package com.revolut.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class JDBCUtils {
	private JDBCUtils() {}

	public static <R> R singleTransaction(ConnectionFactory factory, SQLFunction<Connection, R> action) throws SQLException {
		try (Connection c = factory.getConnection()) {
			c.setAutoCommit(false);
			try {
				R result = action.apply(c);
				c.commit();
				return result;
			} catch (SQLException e) {
				c.rollback();
				throw e;
			} catch (Exception e) {
				c.rollback();
				throw new SQLException("Caught an exception executing JDBCUtils.singleTransaction", e);
			}
		}
	}

//	// To return a result I'll use Callable
//	public static void singleTransaction(ConnectionFactory factory, RunnableWithExsception r) throws SQLException {
//		try (Connection c = factory.getConnection()) {
//			c.setAutoCommit(false);
//			try {
//				r.run();
//				c.commit();
//			} catch (SQLException e) {
//				c.rollback();
//			}
//		}
//	}

	private static <R> List<R> executeSQL(Connection c, SQLFunction<ResultSet, R> parser,
			SQLFunction<Statement, ResultSet> rsSource) throws SQLException {
		Statement statement = c.createStatement();
		try (ResultSet rs = rsSource.apply(statement)) {
			List<R> result = new ArrayList<>();
			if (rs != null) {
				while (rs.next()) {
					R obj = parser.apply(rs);
					result.add(obj);
				};
			}
			return result;
		}
	}

	public static <R> List<R> executeSelect(Connection c, String sql, SQLFunction<ResultSet, R> parser) throws SQLException {
		return executeSQL(c, parser, s -> s.executeQuery(sql));
	}

	public static <R> R executeSelectSingle(Connection c, String sql, SQLFunction<ResultSet, R> parser) throws SQLException {
		List<R> list = executeSQL(c, parser, s -> s.executeQuery(sql));
		int size = list.size();
		if (size != 1) {
			throw new SQLException(String.format("Unexpected resultSet size. Expected:1, actial:%d", size));
		}

		return list.get(0);
	}

	public static <R> R executeInsert(Connection c, String sql, SQLFunction<ResultSet, R> parser, int coulmnsN) throws SQLException {
		int[] columns = new int[coulmnsN];
		for (int i=0; i<coulmnsN; ++i) {
			columns[i] = i+1;
		}

		List<R> list = executeSQL(c, parser, s -> {
			int rows = s.executeUpdate(sql, columns);
			if (rows == 1) {
				return s.getGeneratedKeys();
			}
			return null;
		});

		int size = list.size();
		if (size != 1) {
			throw new SQLException(String.format("Unexpected resultSet size. Expected:1, actial:%d", size));
		}

		return list.get(0);
	}

	public static <R> R executeUpdate(Connection c, String sql, SQLFunction<ResultSet, R> parser, int coulmnsN) throws SQLException {
		return executeInsert(c, sql, parser, coulmnsN);
	}


	public static String stringParam(String s) {
		return s == null ? "null" : ("'" + s + "'");
	}

	@FunctionalInterface
	public static interface SQLFunction<T, R> {
		R apply(T t) throws SQLException;
	}
}
