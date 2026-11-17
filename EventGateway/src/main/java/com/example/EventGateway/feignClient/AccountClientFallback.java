package com.example.EventGateway.feignClient;

import com.example.EventGateway.dto.AccountTransactionRequest;
import com.example.EventGateway.exception.ServiceUnavailableException;
import org.springframework.stereotype.Component;

@Component
public class AccountClientFallback implements AccountClient {

    @Override
    public void applyTransaction(String accountId, AccountTransactionRequest request) {
        throw new ServiceUnavailableException("Account Service is currently unavailable. Transaction for account "
                + accountId + " could not be processed immediately.");
    }
}