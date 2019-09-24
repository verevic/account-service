package com.revolut.account;

import javax.inject.Inject;

import io.micronaut.http.client.RxHttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.MicronautTest;

/**
 * Accounts integration tests
 * 
 * @author verevic
 */
@MicronautTest
public class AccountsIT {
	@Inject
	@Client("/")
	private RxHttpClient client;

	/**
	 * scenario:
	 * - create owner
	 * - create a couple of accounts
	 * - deposit
	 * - withdraw
	 * - transfer
	 */
	public void testAPI() {
		
	}
}
