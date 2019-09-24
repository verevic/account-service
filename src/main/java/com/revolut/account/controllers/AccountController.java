package com.revolut.account.controllers;

import java.util.List;

import javax.inject.Inject;

import com.revolut.ServiceException;
import com.revolut.account.domain.Account;
import com.revolut.account.domain.AccountOperation;
import com.revolut.account.domain.Amount;
import com.revolut.account.service.AccountService;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Put;

@Controller("/accounts")
public class AccountController {
	private final AccountService service;

	@Inject
	public AccountController(AccountService service) {
		this.service = service;
	}

	// deposit, withdraw, transfer
	@Put("/{accountId}/deposit")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Account deposit(long accountId, @Body Amount.Builder builder) throws ServiceException {
		Amount amount = builder.build();
		return service.deposit(accountId, amount);
	}

	@Put("/{accountId}/withdraw")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Account withdraw(long accountId, @Body Amount.Builder builder) throws ServiceException {
		Amount amount = builder.build();
		return service.withdraw(accountId, amount);
	}

	@Put("/{fromId}/{toId}/transfer")
	@Consumes(MediaType.APPLICATION_JSON)
	public void transfer(long fromId, long toId, @Body Amount.Builder builder) throws ServiceException {
		Amount amount = builder.build();
		service.transfer(fromId, toId, amount);
	}

	@Get("/{accountId}/operations")
	@Produces(MediaType.APPLICATION_JSON)
	public List<AccountOperation> listOperations(long accountId) throws ServiceException {
		return service.getOperationsFor(accountId);
	}
}
