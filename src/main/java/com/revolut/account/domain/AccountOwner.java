package com.revolut.account.domain;

public class AccountOwner {
	private final long id;

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

	public String getName() {
		return name;
	}

	public Address getAddress() {
		return address;
	}

	public String getEmail() {
		return email;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof AccountOwner))
			return false;
		AccountOwner other = (AccountOwner) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (id != other.id)
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}


	// Need one for e.g. json parser
	public static class Builder {
		private long id = -1;
		private String name;
		private Address.Builder address;
		private String email;

		public void setId(long id) {
			this.id = id;
		}
		public void setName(String name) {
			this.name = name;
		}
		public void setAddress(Address.Builder address) {
			this.address = address;
		}
		public void setEmail(String email) {
			this.email = email;
		}

		public AccountOwner build() {
			Address addr = address == null ? null : address.build();
			return new AccountOwner(id, name, addr, email);
		}
	}
}
