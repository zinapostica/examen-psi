package com.examenpsi.expensecopilot.config;

import com.examenpsi.expensecopilot.domain.Expense;
import com.examenpsi.expensecopilot.domain.ExpenseCategory;
import com.examenpsi.expensecopilot.domain.ReceiptUpload;
import com.examenpsi.expensecopilot.domain.UploadStatus;
import com.examenpsi.expensecopilot.repository.ExpenseRepository;
import com.examenpsi.expensecopilot.repository.ReceiptUploadRepository;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeedDataRunner {

  private static final Logger log = LoggerFactory.getLogger(SeedDataRunner.class);

  @Bean
  CommandLineRunner seedExpenses(
      ExpenseRepository expenseRepository,
      ReceiptUploadRepository uploadRepository,
      AppProperties props
  ) {
    return args -> {
      if (expenseRepository.count() > 0) {
        return;
      }
      Path dir = Path.of(props.storage().localDir(), "receipts");
      Files.createDirectories(dir);

      seed(expenseRepository, uploadRepository, dir, "Seed Café Luna", LocalDate.now().minusDays(3),
          new BigDecimal("8.90"), ExpenseCategory.FOOD, "Coffee + pastry");
      seed(expenseRepository, uploadRepository, dir, "Metro Ticket", LocalDate.now().minusDays(2),
          new BigDecimal("3.00"), ExpenseCategory.TRANSPORT, "Day pass");
      seed(expenseRepository, uploadRepository, dir, "Office Depot", LocalDate.now().minusDays(10),
          new BigDecimal("24.50"), ExpenseCategory.OFFICE, "Notebooks");
      seed(expenseRepository, uploadRepository, dir, "Hotel Night", LocalDate.now().minusMonths(1).withDayOfMonth(15),
          new BigDecimal("119.00"), ExpenseCategory.TRAVEL, "One night stay");
      seed(expenseRepository, uploadRepository, dir, "Power Co", LocalDate.now().withDayOfMonth(1),
          new BigDecimal("67.20"), ExpenseCategory.UTILITIES, "Monthly bill");
      log.info("Seeded {} demo expenses", expenseRepository.count());
    };
  }

  private static void seed(
      ExpenseRepository expenses,
      ReceiptUploadRepository uploads,
      Path dir,
      String merchant,
      LocalDate date,
      BigDecimal amount,
      ExpenseCategory category,
      String notes
  ) throws Exception {
    UUID id = UUID.randomUUID();
    String key = "receipts/" + id + ".jpg";
    // Minimal valid JPEG (1x1)
    byte[] jpeg = new byte[]{
        (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xD9
    };
    Files.write(dir.resolve(id + ".jpg"), jpeg);

    ReceiptUpload upload = new ReceiptUpload();
    upload.setId(id);
    upload.setObjectKey(key);
    upload.setContentType("image/jpeg");
    upload.setSizeBytes(jpeg.length);
    upload.setStatus(UploadStatus.CONFIRMED);
    uploads.save(upload);

    Expense expense = new Expense();
    expense.setUpload(upload);
    expense.setMerchant(merchant);
    expense.setExpenseDate(date);
    expense.setAmount(amount);
    expense.setCurrency("EUR");
    expense.setCategory(category);
    expense.setNotes(notes);
    expense.setConfidence(0.9);
    expenses.save(expense);
  }
}
