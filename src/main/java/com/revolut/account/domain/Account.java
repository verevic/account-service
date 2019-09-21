package com.revolut.account.domain;

/**
 * Note that an amount could change,
 * So either account or amount should be mutable...
 * Alternatively, I can create a new account instance whenever balance gets changed
 * 
 */
public class Account {
	private final long id;
	private final long ownerId;
	private final Amount amount;

	public Account(long id, long ownerId, Amount amount) {
		this.id = id;
		this.ownerId = ownerId;
		this.amount = amount;
	}

	public long getId() {
		return id;
	}

	public long getOwnerId() {
		return ownerId;
	}

	public Amount getAmount() {
		return amount;
	}

	@Override
	public String toString() {
		return "Account [id=" + id + ", ownerId=" + ownerId + ", amount=" + amount + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Account))
			return false;

		Account other = (Account) obj;
		if (id != other.id || ownerId != other.ownerId)
			return false;

		if (amount == null) {
			return other.amount == null;
		}
		return amount.equals(other.amount);
	}

}
