package com.revolut.account.impl;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.revolut.ServiceException;
import com.revolut.account.domain.AccountOwner;
import com.revolut.account.service.AccountOwnerService;
import com.revolut.impl.db.AccountDBTest;

public class AccountOwnerServiceImplTest extends AccountDBTest {
	private static AccountOwnerService service;

	@BeforeAll
	public static void setupAll() {
		service = new AccountOwnerServiceImpl(getFactory().getConnectionFactory(), getFactory().getAccountOwnerDAO());
	}

	static Object[][] createOwnerParams() {
		return new Object[][] {
			new Object[] {"Victor", "Saburovo park", "verevic@revolut.com"},
			new Object[] {"Victor", "Saburovo park", null},
			new Object[] {"Victor", null, "verevic@revolut.com"},
			new Object[] {"Victor", null, null},
		};
	}

	@ParameterizedTest
	@MethodSource("createOwnerParams")
	public void testCreateAccountOwner(String name, String address, String email) throws ServiceException {
		AccountOwner owner = service.createAccoutOwner(name, address, email);
		Assertions.assertNotNull(owner);
		Assertions.assertEquals(name, owner.getName());
		if (address == null)
			Assertions.assertEquals(address, null);
		else
			Assertions.assertEquals(address, owner.getAddress().getAddress());	
		Assertions.assertEquals(email, owner.getEmail());
	}
}
