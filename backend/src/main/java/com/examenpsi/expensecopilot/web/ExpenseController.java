package com.examenpsi.expensecopilot.web;

import com.examenpsi.expensecopilot.domain.ExpenseCategory;
import com.examenpsi.expensecopilot.service.ExpenseService;
import com.examenpsi.expensecopilot.web.dto.CreateExpenseRequest;
import com.examenpsi.expensecopilot.web.dto.ExpenseResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

  private final ExpenseService expenseService;

  public ExpenseController(ExpenseService expenseService) {
    this.expenseService = expenseService;
  }

  @PostMapping
  public ExpenseResponse create(@Valid @RequestBody CreateExpenseRequest request) {
    return expenseService.create(request);
  }

  @GetMapping
  public List<ExpenseResponse> list(
      @RequestParam(required = false) String month,
      @RequestParam(required = false) ExpenseCategory category
  ) {
    return expenseService.list(month, category);
  }

  @GetMapping("/{id}")
  public ExpenseResponse get(@PathVariable UUID id) {
    return expenseService.get(id);
  }
}
