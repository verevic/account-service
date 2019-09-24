package com.revolut.account.domain;

public class Address {
	private String address;

	public Address(String address) {
		this.address = address;
	}

	public String getAddress() {
		return address;
	}

	@Override
	public String toString() {
		return "Address [address=" + address + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Address))
			return false;

		Address other = (Address) obj;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		return true;
	}

	public static class Builder {
		private String address;
		public String getAddress() {
			return address;
		}
		public void setAddress(String address) {
			this.address = address;
		}

		public Address build() {
			return new Address(address);
		}
	}
}
