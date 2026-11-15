package com.examenpsi.expensecopilot.repository;

import com.examenpsi.expensecopilot.domain.Expense;
import com.examenpsi.expensecopilot.domain.ExpenseCategory;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ExpenseRepository extends MongoRepository<Expense, UUID> {

  List<Expense> findAllByOrderByExpenseDateDescCreatedAtDesc();

  List<Expense> findByCategoryOrderByExpenseDateDescCreatedAtDesc(ExpenseCategory category);

  /**
   * Inclusive start, exclusive end — pass first day of next month as {@code toExclusive}.
   * Uses {@code @Query} because Document criteria cannot stack two operators on the same field
   * via derived query methods.
   */
  @Query(value = "{ 'expenseDate': { $gte: ?0, $lt: ?1 } }", sort = "{ 'expenseDate': -1, 'createdAt': -1 }")
  List<Expense> findInDateRange(LocalDate fromInclusive, LocalDate toExclusive);

  @Query(
      value = "{ 'category': ?0, 'expenseDate': { $gte: ?1, $lt: ?2 } }",
      sort = "{ 'expenseDate': -1, 'createdAt': -1 }")
  List<Expense> findByCategoryInDateRange(
      ExpenseCategory category, LocalDate fromInclusive, LocalDate toExclusive);

  boolean existsByUploadId(UUID uploadId);
}
