package com.revolut.account.domain;

import java.math.BigDecimal;
import java.util.Currency;

/**
 * Still not sure if I need the class instead of just BigDecimal...
 * 
 * I can only perform operations in an account currency...
 * 
 * @author verevic
 *
 */
public class Amount {
	private final Currency currency;
	private final BigDecimal amount;

	public Amount(BigDecimal amount, Currency currency) {
		this.amount = amount;
		this.currency = currency;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public Currency getCurrency() {
		return currency;
	}

	@Override
	public String toString() {
		return "Amount [" + amount + " " + currency.getCurrencyCode() + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Amount))
			return false;

		Amount other = (Amount) obj;
		if ((currency == null || other.currency == null) && currency != other.currency) {
			return false;
		} else if (!currency.getCurrencyCode().equals(other.currency.getCurrencyCode()))
			return false;

		if ((amount == null || other.amount == null) && amount != other.amount) {
			return false;
		} else if (amount.compareTo(other.amount) != 0)
			return false;

		return true;
	}

	// Mutable class for json parser
	public static class Builder {
		private String currency;
		private BigDecimal amount;

		public void setCurrency(String ccy) {
			this.currency = ccy;
		}
		public void setAmount(BigDecimal amount) {
			this.amount = amount;
		}

		public Amount build() {
			return new Amount(amount, Currency.getInstance(currency));
		}
	}
}
