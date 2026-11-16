package com.examenpsi.expensecopilot.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "expenses")
public class Expense {

  @Id
  private UUID id;

  @Indexed(unique = true)
  private UUID uploadId;

  /** Denormalized for list/detail image URLs without an extra join. */
  private String objectKey;

  private String merchant;
  private LocalDate expenseDate;
  private BigDecimal amount;
  private String currency;
  private ExpenseCategory category;
  private String notes;
  private Double confidence;
  private Instant createdAt;
  private Instant updatedAt;

  public void markNew() {
    if (id == null) {
      id = UUID.randomUUID();
    }
    Instant now = Instant.now();
    if (createdAt == null) {
      createdAt = now;
    }
    updatedAt = now;
  }

  public void touch() {
    updatedAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public UUID getUploadId() {
    return uploadId;
  }

  public void setUploadId(UUID uploadId) {
    this.uploadId = uploadId;
  }

  public String getObjectKey() {
    return objectKey;
  }

  public void setObjectKey(String objectKey) {
    this.objectKey = objectKey;
  }

  public String getMerchant() {
    return merchant;
  }

  public void setMerchant(String merchant) {
    this.merchant = merchant;
  }

  public LocalDate getExpenseDate() {
    return expenseDate;
  }

  public void setExpenseDate(LocalDate expenseDate) {
    this.expenseDate = expenseDate;
  }

  public BigDecimal getAmount() {
    return amount;
  }

  public void setAmount(BigDecimal amount) {
    this.amount = amount;
  }

  public String getCurrency() {
    return currency;
  }

  public void setCurrency(String currency) {
    this.currency = currency;
  }

  public ExpenseCategory getCategory() {
    return category;
  }

  public void setCategory(ExpenseCategory category) {
    this.category = category;
  }

  public String getNotes() {
    return notes;
  }

  public void setNotes(String notes) {
    this.notes = notes;
  }

  public Double getConfidence() {
    return confidence;
  }

  public void setConfidence(Double confidence) {
    this.confidence = confidence;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
