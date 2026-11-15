package com.examenpsi.expensecopilot.repository;

import com.examenpsi.expensecopilot.domain.Expense;
import com.examenpsi.expensecopilot.domain.ExpenseCategory;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ExpenseRepository extends MongoRepository<Expense, UUID> {

  List<Expense> findAllByOrderByExpenseDateDescCreatedAtDesc();

  List<Expense> findByCategoryOrderByExpenseDateDescCreatedAtDesc(ExpenseCategory category);

  /** Inclusive start, exclusive end — pass first day of next month as {@code toDateExclusive}. */
  List<Expense> findByExpenseDateGreaterThanEqualAndExpenseDateLessThanOrderByExpenseDateDescCreatedAtDesc(
      LocalDate fromDateInclusive, LocalDate toDateExclusive);

  List<Expense> findByCategoryAndExpenseDateGreaterThanEqualAndExpenseDateLessThanOrderByExpenseDateDescCreatedAtDesc(
      ExpenseCategory category, LocalDate fromDateInclusive, LocalDate toDateExclusive);

  boolean existsByUploadId(UUID uploadId);
}
