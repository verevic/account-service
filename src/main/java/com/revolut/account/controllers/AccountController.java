package com.revolut.account.controllers;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Currency;
import java.util.List;

import com.revolut.account.domain.Account;
import com.revolut.account.domain.Amount;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;

@Controller("/accounts")
public class AccountController {
	@Get("/{ownerId}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Account> listAccounts(long ownerId) {
		return Collections.singletonList(new Account(1, ownerId, new Amount(new BigDecimal(3.5e5), Currency.getInstance("RUB"))));
	}
}
