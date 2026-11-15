package com.examenpsi.expensecopilot.repository;

import com.examenpsi.expensecopilot.domain.Expense;
import com.examenpsi.expensecopilot.domain.ExpenseCategory;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

  List<Expense> findAllByOrderByExpenseDateDescCreatedAtDesc();

  List<Expense> findByCategoryOrderByExpenseDateDescCreatedAtDesc(ExpenseCategory category);

  List<Expense> findByExpenseDateBetweenOrderByExpenseDateDescCreatedAtDesc(
      LocalDate fromDate, LocalDate toDate);

  List<Expense> findByCategoryAndExpenseDateBetweenOrderByExpenseDateDescCreatedAtDesc(
      ExpenseCategory category, LocalDate fromDate, LocalDate toDate);

  boolean existsByUploadId(UUID uploadId);
}
