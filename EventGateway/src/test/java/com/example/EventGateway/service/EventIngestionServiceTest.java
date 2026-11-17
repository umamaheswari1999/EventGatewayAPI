package com.example.EventGateway.service;

import com.example.EventGateway.dto.AccountTransactionRequest;
import com.example.EventGateway.entity.Event;
import com.example.EventGateway.exception.ServiceUnavailableException;
import com.example.EventGateway.feignClient.AccountClient;
import com.example.EventGateway.repo.EventGatewayRepo;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class EventIngestionServiceTest {

    private final EventGatewayRepo repository = mock(EventGatewayRepo.class);
    private final AccountClient accountClient = mock(AccountClient.class);
    private final EventIngestionService service = new EventIngestionService(repository, accountClient);

    @Test
    void forwardsEventAsAccountTransaction() {
        Event event = Event.builder()
                .eventId("event-1")
                .accountId("account-1")
                .type("DEBIT")
                .amount(new BigDecimal("9.25"))
                .eventTimestamp(Instant.parse("2025-01-02T03:04:05Z"))
                .build();

        Event result = service.processAndForwardEvent(event);

        assertSame(event, result);
        verify(accountClient).applyTransaction(eq("account-1"), eq(new AccountTransactionRequest(
                "event-1",
                new BigDecimal("9.25"),
                "DEBIT",
                Instant.parse("2025-01-02T03:04:05Z")
        )));
        verifyNoInteractions(repository);
    }

    @Test
    void fallbackPreservesServiceUnavailableException() {
        Event event = Event.builder().accountId("account-1").build();
        ServiceUnavailableException original =
                new ServiceUnavailableException("Account service is down.");

        ServiceUnavailableException exception = assertThrows(
                ServiceUnavailableException.class,
                () -> service.handleUnavailable(event, original));

        assertSame(original, exception);
    }

    @Test
    void fallbackConvertsOtherFailuresToServiceUnavailable() {
        Event event = Event.builder().accountId("account-1").build();

        ServiceUnavailableException exception = assertThrows(
                ServiceUnavailableException.class,
                () -> service.handleUnavailable(event, new IllegalStateException("Connection refused")));

        assertEquals(
                "Account Service is currently unavailable. Transaction for account account-1 could not be processed immediately.",
                exception.getMessage());
    }
}
