package com.revolut.account.controllers;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import com.revolut.account.domain.Account;
import com.revolut.account.domain.AccountOwner;
import com.revolut.account.domain.Address;
import com.revolut.account.domain.Amount;
import com.revolut.account.service.AccountOwnerService;
import com.revolut.account.service.AccountService;
import com.revolut.exception.ServiceException;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;

@Controller("/owners")
public class AccountOwnerController {
	private final AccountOwnerService ownerService;
	private final AccountService accountService;

	@Inject
	public AccountOwnerController(AccountOwnerService ownerService, AccountService accountService) {
		this.ownerService = ownerService;
		this.accountService = accountService;
	}

	@Get
	@Produces(MediaType.APPLICATION_JSON)
	public List<AccountOwner> listOwners() throws ServiceException {
		return ownerService.getOwners();
	}

	@Post
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public AccountOwner createAccountOwner(@Body AccountOwner.Builder builder) throws ServiceException {
		AccountOwner owner = builder.build();
		String address = Optional.ofNullable(owner.getAddress())
				.map(Address::getAddress)
				.orElse(null);
		return ownerService.createOwner(owner.getName(), address, owner.getEmail());
	}

	@Get("/{ownerId}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Account> listAccounts(long ownerId) throws ServiceException {
		return accountService.getAccountsFor(ownerId);
	}

	@Post("/{ownerId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Account createAccount(long ownerId, @Body Amount.Builder amountBuilder) throws ServiceException {
		Amount amount = amountBuilder.build();
		return accountService.createAccount(ownerId, amount);
	}
}
