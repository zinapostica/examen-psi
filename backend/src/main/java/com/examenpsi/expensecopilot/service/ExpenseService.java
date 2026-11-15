package com.examenpsi.expensecopilot.service;

import com.examenpsi.expensecopilot.domain.Expense;
import com.examenpsi.expensecopilot.domain.ExpenseCategory;
import com.examenpsi.expensecopilot.domain.ReceiptUpload;
import com.examenpsi.expensecopilot.domain.UploadStatus;
import com.examenpsi.expensecopilot.repository.ExpenseRepository;
import com.examenpsi.expensecopilot.storage.ObjectStorage;
import com.examenpsi.expensecopilot.web.dto.CreateExpenseRequest;
import com.examenpsi.expensecopilot.web.dto.ExpenseResponse;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpenseService {

  private final ExpenseRepository expenseRepository;
  private final ReceiptService receiptService;
  private final ObjectStorage objectStorage;

  public ExpenseService(
      ExpenseRepository expenseRepository,
      ReceiptService receiptService,
      ObjectStorage objectStorage
  ) {
    this.expenseRepository = expenseRepository;
    this.receiptService = receiptService;
    this.objectStorage = objectStorage;
  }

  @Transactional
  public ExpenseResponse create(CreateExpenseRequest request) {
    if (expenseRepository.existsByUploadId(request.uploadId())) {
      throw new IllegalStateException("An expense already exists for this upload");
    }
    ReceiptUpload upload = receiptService.requireUpload(request.uploadId());
    if (upload.getStatus() == UploadStatus.CONFIRMED) {
      throw new IllegalStateException("Upload already confirmed");
    }

    Expense expense = new Expense();
    expense.setUpload(upload);
    expense.setMerchant(request.merchant().trim());
    expense.setExpenseDate(request.expenseDate());
    expense.setAmount(request.amount());
    expense.setCurrency(request.currency().toUpperCase());
    expense.setCategory(request.category());
    expense.setNotes(request.notes());
    expense.setConfidence(request.confidence());
    expenseRepository.save(expense);

    receiptService.markConfirmed(upload);
    return toResponse(expense);
  }

  @Transactional(readOnly = true)
  public List<ExpenseResponse> list(String month, ExpenseCategory category) {
    boolean hasMonth = month != null && !month.isBlank();
    boolean hasCategory = category != null;
    List<Expense> expenses;
    if (hasMonth && hasCategory) {
      YearMonth ym = YearMonth.parse(month);
      expenses = expenseRepository.findByCategoryAndExpenseDateBetweenOrderByExpenseDateDescCreatedAtDesc(
          category, ym.atDay(1), ym.atEndOfMonth());
    } else if (hasMonth) {
      YearMonth ym = YearMonth.parse(month);
      expenses = expenseRepository.findByExpenseDateBetweenOrderByExpenseDateDescCreatedAtDesc(
          ym.atDay(1), ym.atEndOfMonth());
    } else if (hasCategory) {
      expenses = expenseRepository.findByCategoryOrderByExpenseDateDescCreatedAtDesc(category);
    } else {
      expenses = expenseRepository.findAllByOrderByExpenseDateDescCreatedAtDesc();
    }
    return expenses.stream().map(this::toResponse).toList();
  }

  @Transactional(readOnly = true)
  public ExpenseResponse get(UUID id) {
    Expense expense = expenseRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Expense not found: " + id));
    return toResponse(expense);
  }

  private ExpenseResponse toResponse(Expense expense) {
    return new ExpenseResponse(
        expense.getId(),
        expense.getUpload().getId(),
        expense.getMerchant(),
        expense.getExpenseDate(),
        expense.getAmount(),
        expense.getCurrency(),
        expense.getCategory(),
        expense.getNotes(),
        expense.getConfidence(),
        objectStorage.resolveUrl(expense.getUpload().getObjectKey()),
        expense.getCreatedAt(),
        expense.getUpdatedAt()
    );
  }
}
