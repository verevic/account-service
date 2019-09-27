package com.revolut.account.controller;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.revolut.account.domain.Account;
import com.revolut.account.domain.AccountOperation;
import com.revolut.account.domain.AccountOwner;
import com.revolut.account.domain.Address;
import com.revolut.account.domain.Amount;
import com.revolut.exception.ServiceExceptionError;

import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.annotation.MicronautTest;

/**
 * Tests for the AccountController
 * 
 * @author verevic
 */
@MicronautTest
public class AccountControllerTest {
	@Inject
	@Client("/")
	private RxHttpClient client;

	private AccountOwner createOwner(String name, String address, String email) {
		Address addr = address == null ? null : new Address(address);
		HttpRequest<AccountOwner> request = HttpRequest.POST("/owners", new AccountOwner(-1, name, addr, email));
		AccountOwner.Builder builder = client.toBlocking().retrieve(request, AccountOwner.Builder.class);
		Assertions.assertNotNull(builder);
		return builder.build();
	}

	private Account createAccount(long ownerId, BigDecimal amount, Currency ccy) {
		HttpRequest<Amount> request = HttpRequest.POST(String.format("/owners/%d/createAccount", ownerId), new Amount(amount, ccy));
		Account.Builder builder = client.toBlocking().retrieve(request, Account.Builder.class);
		Assertions.assertNotNull(builder);
		return builder.build();
	}

	private List<Account> getAccounts(long ownerId) {
		HttpRequest<Void> request =  HttpRequest.GET(String.format("owners/%d/listAccounts", ownerId));
		List<Account.Builder> builders = client.toBlocking().retrieve(request, Argument.listOf(Account.Builder.class));
		return builders.stream().map(Account.Builder::build).collect(Collectors.toList());
	}

	private List<AccountOperation> getOperations(long accountId) {
		HttpRequest<Void> request = HttpRequest.GET(String.format("accounts/%d/operations", accountId));
		List<AccountOperation.Builder> builders = client.toBlocking().retrieve(request, Argument.listOf(AccountOperation.Builder.class));
		return builders.stream().map(AccountOperation.Builder::build).collect(Collectors.toList());
	}

	@Test
	public void testDeposit() {
		AccountOwner owner = createOwner("Victor", "Saburovo park", "verevic@revolut.com");

		BigDecimal initial = new BigDecimal(100000);
		Currency ccy = Currency.getInstance("RUB");
		Account account = createAccount(owner.getId(), initial, ccy);

		BigDecimal deposit = new BigDecimal(50000);
		HttpRequest<Amount> request = HttpRequest.PUT(String.format("/accounts/%d/deposit", account.getId()), new Amount(deposit, ccy));
		Account.Builder builder = client.toBlocking().retrieve(request, Account.Builder.class);
		Assertions.assertNotNull(builder);
		Account changed = builder.build();
		Assertions.assertEquals(account.getId(), changed.getId());
		Assertions.assertEquals(owner.getId(), account.getOwnerId());
		BigDecimal expected = initial.add(deposit);
		Assertions.assertEquals(0, expected.compareTo(changed.getAmount().getAmount()));

		// operations
		List<AccountOperation> operations = getOperations(account.getId());
		Assertions.assertEquals(2,  operations.size());
		AccountOperation op = operations.get(1);
		Assertions.assertEquals(0, expected.compareTo(op.getBalance().getAmount()));
		Assertions.assertEquals(ccy, op.getBalance().getCurrency());
	}

	@Test
	public void testWithdraw() {
		AccountOwner owner = createOwner("Victor", "Saburovo park", "verevic@revolut.com");

		BigDecimal initial = new BigDecimal(100000);
		Currency ccy = Currency.getInstance("RUB");
		Account account = createAccount(owner.getId(), initial, ccy);

		BigDecimal debit = new BigDecimal(50000);
		HttpRequest<Amount> request = HttpRequest.PUT(String.format("/accounts/%d/withdraw", account.getId()), new Amount(debit, ccy));
		Account.Builder builder = client.toBlocking().retrieve(request, Account.Builder.class);
		Assertions.assertNotNull(builder);
		Account changed = builder.build();
		Assertions.assertEquals(account.getId(), changed.getId());
		Assertions.assertEquals(owner.getId(), account.getOwnerId());
		BigDecimal expected = initial.subtract(debit);
		Assertions.assertEquals(0, expected.compareTo(changed.getAmount().getAmount()));

		// operations
		List<AccountOperation> operations = getOperations(account.getId());
		Assertions.assertEquals(2,  operations.size());
		AccountOperation op = operations.get(1);
		Assertions.assertEquals(0, expected.compareTo(op.getBalance().getAmount()));
		Assertions.assertEquals(ccy, op.getBalance().getCurrency());
	}

	@Test
	public void testTransfer() {
		AccountOwner owner = createOwner("Victor", "Saburovo park", "verevic@revolut.com");
		
		Currency ccy = Currency.getInstance("RUB");
		BigDecimal initialFrom = new BigDecimal(100_000);
		Account from = createAccount(owner.getId(), initialFrom, ccy);
		
		BigDecimal initialTo = new BigDecimal(70_000);
		Account to = createAccount(owner.getId(), initialTo, ccy);

		BigDecimal transfer = new BigDecimal(50_000);
		BigDecimal fromAmount = initialFrom.subtract(transfer);
		BigDecimal toAmount = initialTo.add(transfer);

		HttpRequest<Amount> request =
				HttpRequest.PUT(String.format("/accounts/%d/%d/transfer", from.getId(), to.getId()), new Amount(transfer, ccy));
		HttpResponse<Void> response = client.toBlocking().exchange(request);
		Assertions.assertEquals(HttpStatus.OK, response.getStatus());
		// accounts
		List<Account> accounts = getAccounts(owner.getId());
		Assertions.assertEquals(2, accounts.size());
		Account newFrom = accounts.get(0);
		Assertions.assertEquals(from.getId(), newFrom.getId());
		Assertions.assertEquals(0, fromAmount.compareTo(newFrom.getAmount().getAmount()));
		Assertions.assertEquals(ccy, newFrom.getAmount().getCurrency());

		Account newTo = accounts.get(1);
		Assertions.assertEquals(to.getId(), newTo.getId());
		Assertions.assertEquals(0, toAmount.compareTo(newTo.getAmount().getAmount()));
		Assertions.assertEquals(ccy, newTo.getAmount().getCurrency());

		// operations
		List<AccountOperation> operations = getOperations(from.getId());
		Assertions.assertEquals(2, operations.size());
		AccountOperation op = operations.get(1);
		Assertions.assertEquals(0, fromAmount.compareTo(op.getBalance().getAmount()));
		Assertions.assertEquals(ccy, op.getBalance().getCurrency());

		operations = getOperations(to.getId());
		Assertions.assertEquals(2, operations.size());
		op = operations.get(1);
		Assertions.assertEquals(0, toAmount.compareTo(op.getBalance().getAmount()));
		Assertions.assertEquals(ccy, op.getBalance().getCurrency());
	}

	@Test
	public void testWithdrawWithDifferentCcy() {
		AccountOwner owner = createOwner("Victor", "Saburovo park", "verevic@revolut.com");

		BigDecimal initial = new BigDecimal(100000);
		Currency rub = Currency.getInstance("RUB");
		Account account = createAccount(owner.getId(), initial, rub);

		BigDecimal debit = new BigDecimal(50000);
		Currency eur = Currency.getInstance("EUR");
		Amount withdraw = new Amount(debit, eur);

		HttpRequest<Amount> request = HttpRequest.PUT(String.format("/accounts/%d/withdraw", account.getId()), withdraw);
		try {
			client.toBlocking().retrieve(request, Argument.of(Account.Builder.class),
					Argument.of(ServiceExceptionError.Builder.class));
			Assertions.fail("BadRequest response is expected");
		} catch (HttpClientResponseException e) {
			// TODO: check if there's a better way of testing BadException...
			HttpResponse<?> response = e.getResponse();
			Assertions.assertNotNull(response);
			ServiceExceptionError.Builder builder = response.getBody(ServiceExceptionError.Builder.class).get();
			ServiceExceptionError err = builder.build();
			Assertions.assertEquals(String.format("Failed to withdraw %s from accountId:%d", withdraw, account.getId()),
					err.getMsg());
			Assertions.assertEquals(String.format("Account(%d) currency(%s) doesn't match operation currency(%s)", account.getId(), rub, eur),
					err.getReason());
		}
	}

	@Test
	public void testWithdrawWithOverdraft() {
		AccountOwner owner = createOwner("Victor", "Saburovo park", "verevic@revolut.com");

		BigDecimal initial = new BigDecimal(100_000);
		Currency rub = Currency.getInstance("RUB");
		Account account = createAccount(owner.getId(), initial, rub);

		BigDecimal debit = new BigDecimal(100_001);
		Amount withdraw = new Amount(debit, rub);

		HttpRequest<Amount> request = HttpRequest.PUT(String.format("/accounts/%d/withdraw", account.getId()), withdraw);
		try {
			client.toBlocking().retrieve(request, null, Argument.of(ServiceExceptionError.Builder.class));
			Assertions.fail("BadRequest response is expected");
		} catch (HttpClientResponseException e) {
			// TODO: check if there's a better way of testing BadException...
			HttpResponse<?> response = e.getResponse();
			Assertions.assertNotNull(response);
			ServiceExceptionError.Builder builder = response.getBody(ServiceExceptionError.Builder.class).get();
			ServiceExceptionError err = builder.build();
			Assertions.assertEquals(String.format("Failed to withdraw %s from accountId:%d", withdraw, account.getId()),
					err.getMsg());
			Assertions.assertEquals(String.format("Account(%d) balance cannot be negative", account.getId()), err.getReason());
		}
	}

	@Test
	public void testTransferWithOverdraft() {
		AccountOwner owner = createOwner("Victor", "Saburovo park", "verevic@revolut.com");
		
		Currency ccy = Currency.getInstance("RUB");
		BigDecimal initialFrom = new BigDecimal(100_000);
		Account from = createAccount(owner.getId(), initialFrom, ccy);
		
		BigDecimal initialTo = new BigDecimal(70_000);
		Account to = createAccount(owner.getId(), initialTo, ccy);

		BigDecimal transfer = new BigDecimal(200_000);

		HttpRequest<Amount> request =
				HttpRequest.PUT(String.format("/accounts/%d/%d/transfer", from.getId(), to.getId()), new Amount(transfer, ccy));
		try {
			client.toBlocking().exchange(request, null, Argument.of(ServiceExceptionError.Builder.class));
			Assertions.fail("BadRequest response is expected");
		} catch (HttpClientResponseException e) {
			// accounts
			List<Account> accounts = getAccounts(owner.getId());
			Assertions.assertEquals(2, accounts.size());
			Account newFrom = accounts.get(0);
			Assertions.assertEquals(from.getId(), newFrom.getId());
			Assertions.assertEquals(0, initialFrom.compareTo(newFrom.getAmount().getAmount()));
			Assertions.assertEquals(ccy, newFrom.getAmount().getCurrency());

			Account newTo = accounts.get(1);
			Assertions.assertEquals(to.getId(), newTo.getId());
			Assertions.assertEquals(0, initialTo.compareTo(newTo.getAmount().getAmount()));
			Assertions.assertEquals(ccy, newTo.getAmount().getCurrency());

			// operations
			List<AccountOperation> operations = getOperations(from.getId());
			Assertions.assertEquals(1, operations.size());

			operations = getOperations(to.getId());
			Assertions.assertEquals(1, operations.size());
		}
	}
}
