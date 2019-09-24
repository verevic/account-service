package com.revolut.account.controller;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.revolut.account.domain.Account;
import com.revolut.account.domain.AccountOwner;
import com.revolut.account.domain.Address;
import com.revolut.account.domain.Amount;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.MicronautTest;

@MicronautTest
public class AccountOwnerControllerTest {
	@Inject
	@Client("/owners")
	private RxHttpClient client;

	@ParameterizedTest
	@MethodSource("com.revolut.account.service.AccountOwnerServiceTest#createOwnerParams")
	public void testCreateOwner(String name, String address, String email) {
		AccountOwner owner = createOwner(name, address, email);
		Assertions.assertTrue(owner.getId() >= 0);
		Assertions.assertEquals(name, owner.getName());
		Address addr = address == null ? null : new Address(address);
		Assertions.assertEquals(addr, owner.getAddress());
		Assertions.assertEquals(email, owner.getEmail());
	}

	private AccountOwner createOwner(String name, String address, String email) {
		Address addr = address == null ? null : new Address(address);
		HttpRequest<AccountOwner> request = HttpRequest.POST("/", new AccountOwner(-1, name, addr, email));
		AccountOwner.Builder builder = client.toBlocking().retrieve(request, AccountOwner.Builder.class);
		Assertions.assertNotNull(builder);
		return builder.build();
	}

	@Test
	public void testListOwners() {
		AccountOwner owner = createOwner("Victor", "Saburovo park", "verevic@revolut.com");
		HttpRequest<Void> request = HttpRequest.GET("/");
		List<AccountOwner.Builder> builders = client.toBlocking().retrieve(request, Argument.listOf(AccountOwner.Builder.class));
		Assertions.assertNotNull(builders);
		AccountOwner returned = builders.get(builders.size() - 1).build();
		Assertions.assertEquals(owner, returned);
	}

	private Account createAccount(long ownerId, BigDecimal amount, Currency ccy) {
		HttpRequest<Amount> request = HttpRequest.POST(String.format("/%d", ownerId), new Amount(amount, ccy));
		Account.Builder builder = client.toBlocking().retrieve(request, Account.Builder.class);
		Assertions.assertNotNull(builder);
		return builder.build();
	}

	@Test
	public void testCreateAccount() {
		AccountOwner owner = createOwner("Victor", "Saburovo park", "verevic@revolut.com");
		BigDecimal amount = new BigDecimal(350000);
		Currency ccy = Currency.getInstance("RUB");
		Account account = createAccount(owner.getId(), amount, ccy);
		Assertions.assertTrue(account.getId() >= 0);
		Assertions.assertEquals(owner.getId(), account.getOwnerId());
		Assertions.assertEquals(0, amount.compareTo(account.getAmount().getAmount()));
		Assertions.assertEquals(ccy, account.getAmount().getCurrency());
	}

	@Test
	public void testListAccounts() {
		AccountOwner owner = createOwner("Victor", "Saburovo park", "verevic@revolut.com");
		BigDecimal amount = new BigDecimal(350000);
		Currency ccy = Currency.getInstance("RUB");
		Account account = createAccount(owner.getId(), amount, ccy);
		// list
		HttpRequest<Void> request = HttpRequest.GET(String.format("/%d", owner.getId()));
		List<Account.Builder> builders = client.toBlocking().retrieve(request, Argument.listOf(Account.Builder.class));
		Assertions.assertNotNull(builders);
		Account returned = builders.get(builders.size() - 1).build();
		Assertions.assertEquals(account, returned);
	}
}
