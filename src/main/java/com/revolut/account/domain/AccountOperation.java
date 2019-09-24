package com.revolut.account.domain;

import java.util.Date;

/**
 * A user (account owner) would probably be mostly interested in
 * - a balance
 * - operations history
 * 
 * It doesn't make much sense to mix operations for different accounts
 * 
 * @author verevic
 *
 */
public class AccountOperation {
	private final long id;
	private final long accountId;
	private final Date timestamp;
	private final String details;
	private final Amount balance;	// resulting balance

	public AccountOperation(long id, long accountId, Date timestamp, String details, Amount balance) {
		this.id = id;
		this.accountId = accountId;
		this.timestamp = timestamp;
		this.details = details;
		this.balance = balance;
	}

	public long getId() {
		return id;
	}

	public long getAccountId() {
		return accountId;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public String getDetails() {
		return details;
	}

	public Amount getBalance() {
		return balance;
	}
}
