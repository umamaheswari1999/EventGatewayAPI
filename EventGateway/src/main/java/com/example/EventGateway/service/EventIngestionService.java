package com.example.EventGateway.service;

import com.example.EventGateway.dto.AccountTransactionRequest;
import com.example.EventGateway.entity.Event;
import com.example.EventGateway.exception.ServiceUnavailableException;
import com.example.EventGateway.feignClient.AccountClient;
import com.example.EventGateway.repo.EventGatewayRepo;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class EventIngestionService {

    private final EventGatewayRepo eventRepository;
    private final AccountClient accountClient;

    public EventIngestionService(EventGatewayRepo eventRepository, AccountClient accountClient) {
        this.eventRepository = eventRepository;
        this.accountClient = accountClient;
    }

    @Transactional
    @CircuitBreaker(name = "default", fallbackMethod = "handleUnavailable")
    @Bulkhead(name = "default", type = Bulkhead.Type.SEMAPHORE, fallbackMethod = "handleUnavailable")
    public Event processAndForwardEvent(Event dto) {
        AccountTransactionRequest accountRequest = new AccountTransactionRequest(
                dto.getEventId(),
                dto.getAmount(),
                dto.getType(),
                dto.getEventTimestamp()
        );

        accountClient.applyTransaction(dto.getAccountId(), accountRequest);
        log.info("Event forwarded to Account Service: {}", dto.getAccountId());
        return dto;
    }

    public Event handleUnavailable(Event dto, Throwable exception) {
        if (exception instanceof ServiceUnavailableException serviceUnavailableException) {
            throw serviceUnavailableException;
        }

        throw new ServiceUnavailableException("Account Service is currently unavailable. Transaction for account "
                + dto.getAccountId() + " could not be processed immediately.");
    }
}