package com.example.EventGateway.feignClient;

import com.example.EventGateway.dto.AccountTransactionRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
    name = "account-service", 
    url = "${account-service.url}", 
    fallback = AccountClientFallback.class
)
public interface AccountClient {

    @PostMapping("/accounts/{accountId}/transactions")
    void applyTransaction(
            @PathVariable("accountId") String accountId,
            @RequestBody AccountTransactionRequest request
    );
}

