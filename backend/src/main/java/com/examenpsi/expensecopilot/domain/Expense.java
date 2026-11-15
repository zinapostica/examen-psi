package com.examenpsi.expensecopilot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "expenses")
public class Expense implements Persistable<UUID> {

  @Id
  private UUID id;

  @Transient
  private boolean newEntity = true;

  @OneToOne(optional = false)
  @JoinColumn(name = "upload_id", nullable = false, unique = true)
  private ReceiptUpload upload;

  @Column(nullable = false)
  private String merchant;

  @Column(nullable = false)
  private LocalDate expenseDate;

  @Column(nullable = false, precision = 12, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false, length = 3)
  private String currency;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private ExpenseCategory category;

  @Column(length = 1000)
  private String notes;

  private Double confidence;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @Column(nullable = false)
  private Instant updatedAt;

  @PrePersist
  void onCreate() {
    if (id == null) {
      id = UUID.randomUUID();
    }
    Instant now = Instant.now();
    if (createdAt == null) {
      createdAt = now;
    }
    updatedAt = now;
    newEntity = false;
  }

  @PostLoad
  void markNotNew() {
    newEntity = false;
  }

  @Override
  public boolean isNew() {
    return newEntity;
  }

  @PreUpdate
  void onUpdate() {
    updatedAt = Instant.now();
  }

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public ReceiptUpload getUpload() {
    return upload;
  }

  public void setUpload(ReceiptUpload upload) {
    this.upload = upload;
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
