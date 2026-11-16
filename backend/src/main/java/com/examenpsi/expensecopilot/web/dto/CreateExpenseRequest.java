package com.examenpsi.expensecopilot.web.dto;

import com.examenpsi.expensecopilot.domain.ExpenseCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CreateExpenseRequest(
    @NotNull UUID uploadId,
    @NotBlank @Size(max = 200) String merchant,
    @NotNull LocalDate expenseDate,
    @NotNull @DecimalMin(value = "0.01", inclusive = true) BigDecimal amount,
    @NotBlank @Pattern(regexp = "[A-Z]{3}") String currency,
    @NotNull ExpenseCategory category,
    @Size(max = 1000) String notes,
    Double confidence
) {}
