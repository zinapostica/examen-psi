package com.examenpsi.expensecopilot.web.dto;

import com.examenpsi.expensecopilot.domain.ExpenseCategory;
import com.examenpsi.expensecopilot.domain.UploadStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record ReceiptUploadResponse(
    UUID id,
    UploadStatus status,
    String contentType,
    long sizeBytes,
    String imageUrl,
    String merchant,
    LocalDate expenseDate,
    BigDecimal amount,
    String currency,
    ExpenseCategory category,
    Double confidence,
    String rawExtractJson,
    Instant createdAt
) {}
