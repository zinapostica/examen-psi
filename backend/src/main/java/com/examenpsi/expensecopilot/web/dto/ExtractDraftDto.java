package com.examenpsi.expensecopilot.web.dto;

import com.examenpsi.expensecopilot.domain.ExpenseCategory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ExtractDraftDto(
    String merchant,
    LocalDate expenseDate,
    BigDecimal amount,
    String currency,
    ExpenseCategory category,
    Double confidence,
    List<LineItemDto> lineItems
) {
  public record LineItemDto(String description, BigDecimal amount) {}
}
