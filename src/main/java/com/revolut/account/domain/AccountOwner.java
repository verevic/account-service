package com.revolut.account.domain;

public class AccountOwner {
	private long id;

	private final String name;
	private final Address address;
	private final String email;
	// phone, etc...

	public AccountOwner(long id, String name, Address address, String email) {
		this.id = id;
		this.name = name;
		this.address = address;
		this.email = email;
	}

	public long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public Address getAddress() {
		return address;
	}

	public String getEmail() {
		return email;
	}
}
