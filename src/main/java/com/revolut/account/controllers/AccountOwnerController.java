package com.revolut.account.controllers;

import java.util.Collections;
import java.util.List;

import com.revolut.account.domain.AccountOwner;
import com.revolut.account.domain.Address;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;

@Controller("/owners")
public class AccountOwnerController {
	
	
	
	@Get
	@Produces(MediaType.APPLICATION_JSON)
	public List<AccountOwner> listOwners() {
		return Collections.singletonList(new AccountOwner(123, "Victor", new Address("Saburovo park"), "verevic@revolut.com"));
	}

}
