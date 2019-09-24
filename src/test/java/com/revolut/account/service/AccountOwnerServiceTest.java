package com.revolut.account.service;

import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.revolut.ServiceException;
import com.revolut.account.domain.AccountOwner;
import com.revolut.account.service.AccountOwnerService;
import com.revolut.dao.DatabaseTest;

import io.micronaut.test.annotation.MicronautTest;

@MicronautTest
public class AccountOwnerServiceTest extends DatabaseTest {
	@Inject
	private AccountOwnerService service;

	public static Object[][] createOwnerParams() {
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
		AccountOwner owner = service.createOwner(name, address, email);
		Assertions.assertNotNull(owner);
		Assertions.assertTrue(owner.getId() >= 0);
		Assertions.assertEquals(name, owner.getName());
		if (address == null)
			Assertions.assertEquals(address, null);
		else
			Assertions.assertEquals(address, owner.getAddress().getAddress());	
		Assertions.assertEquals(email, owner.getEmail());
	}

	@Test
	public void testGetOwners() throws ServiceException {
		List<AccountOwner> owners = service.getOwners();
		Assertions.assertNotNull(owners);
		Assertions.assertEquals(0, owners.size());

		AccountOwner owner = service.createOwner("Victor", "Saburovo park", "verevic@revolut.com");

		owners = service.getOwners();
		Assertions.assertNotNull(owners);
		Assertions.assertEquals(1, owners.size());
		Assertions.assertEquals(owner, owners.get(0));
	}
}
