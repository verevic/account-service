package com.revolut.account.dao;

import java.sql.SQLException;
import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.revolut.account.domain.AccountOwner;
import com.revolut.account.domain.Address;
import com.revolut.dao.DatabaseTest;
import com.revolut.dao.TransactionManager;
import com.revolut.exception.BusinessRuleException;

public class AccountOwnerDAOTest extends DatabaseTest {
	@Inject
	private TransactionManager transactionManager;

	static Object[][] createOwnerParams() {
		return new Object[][] {
			new Object[] {"Victor", new Address("Saburovo park"), "verevic@revolut.com"},
			new Object[] {"Victor", new Address(null), "verevic@revolut.com"},
			new Object[] {"Victor", null, "verevic@revolut.com"},
			new Object[] {"Victor", "Saburovo park", null},
			new Object[] {"Victor", null, null},
		};
	}

	@ParameterizedTest
	@MethodSource("createOwnerParams")
	public void testCreateOwner(String name, Address address, String email) throws SQLException, BusinessRuleException {
		AccountOwner owner = transactionManager.runWithResult(c -> AccountOwnerDAO.createOwner(c, new AccountOwner(-1, name, address, email)));
		Assertions.assertNotNull(owner);
		Assertions.assertNotEquals(-1, owner.getId());
		Assertions.assertEquals(name, owner.getName());
		if (address == null || address.getAddress() == null)
			Assertions.assertNull(owner.getAddress());
		else
			Assertions.assertEquals(address.getAddress(), owner.getAddress().getAddress());
		Assertions.assertEquals(email, owner.getEmail());
	}

	@Test
	public void testGetOwners() throws SQLException, BusinessRuleException {
		List<AccountOwner> owners = transactionManager.runWithResult(c -> AccountOwnerDAO.list(c));
		Assertions.assertNotNull(owners);
		Assertions.assertEquals(0, owners.size());
		
		String name = "New owner";
		Address address = new Address("New owner address");
		String email = "new.owner@revolut.com";
		transactionManager.runWithResult(c -> AccountOwnerDAO.createOwner(c, new AccountOwner(-1, name, address, email)));

		owners = transactionManager.runWithResult(c -> AccountOwnerDAO.list(c));
		Assertions.assertNotNull(owners);
		Assertions.assertEquals(1, owners.size());
		AccountOwner owner = owners.get(0);
		Assertions.assertEquals(name, owner.getName());
		Assertions.assertEquals(address.getAddress(), owner.getAddress().getAddress());
		Assertions.assertEquals(email, owner.getEmail());
	}
}
