package com.example.EventGateway.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventDto {

    @NotBlank
    private String eventId;

    @NotBlank
    private String accountId;

    @NotBlank
    @Pattern(regexp = "CREDIT|DEBIT")
    private String type;

    @NotNull
    @Positive
    private BigDecimal amount;

    @NotBlank
    private String currency;

    @NotBlank
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}(\\.\\d+)?(Z|[+-]\\d{2}:\\d{2})$")
    private String eventTimestamp;

    private Map<String, Object> metadata;
}
