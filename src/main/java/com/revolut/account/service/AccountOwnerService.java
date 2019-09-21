package com.revolut.account.service;

import java.util.List;

import com.revolut.ServiceException;
import com.revolut.account.domain.AccountOwner;

public interface AccountOwnerService {
	AccountOwner createAccoutOwner(String name, String address, String email) throws ServiceException;
	List<AccountOwner> getOwners() throws ServiceException;
}
