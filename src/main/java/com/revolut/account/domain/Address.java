package com.revolut.account.domain;

public class Address {
	private final String address;

	public Address(String address) {
		super();
		this.address = address;
	}

	public String getAddress() {
		return address;
	}

	@Override
	public String toString() {
		return "Address [address=" + address + "]";
	}
}
