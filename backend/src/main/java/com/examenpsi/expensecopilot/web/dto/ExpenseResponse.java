package com.examenpsi.expensecopilot.web.dto;

import com.examenpsi.expensecopilot.domain.ExpenseCategory;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ExpenseResponse(
    UUID id,
    UUID uploadId,
    String merchant,
    LocalDate expenseDate,
    BigDecimal amount,
    String currency,
    ExpenseCategory category,
    String notes,
    Double confidence,
    String imageUrl,
    Instant createdAt,
    Instant updatedAt
) {}
