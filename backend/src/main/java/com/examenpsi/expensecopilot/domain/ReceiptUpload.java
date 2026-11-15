package com.examenpsi.expensecopilot.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Persistable;

@Entity
@Table(name = "receipt_uploads")
public class ReceiptUpload implements Persistable<UUID> {

  @Id
  private UUID id;

  @Transient
  private boolean newEntity = true;

  @Column(nullable = false)
  private String objectKey;

  @Column(nullable = false)
  private String contentType;

  @Column(nullable = false)
  private long sizeBytes;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 32)
  private UploadStatus status;

  @Column(columnDefinition = "TEXT")
  private String rawExtractJson;

  @Column(columnDefinition = "TEXT")
  private String draftMerchant;

  private java.time.LocalDate draftExpenseDate;

  @Column(precision = 12, scale = 2)
  private java.math.BigDecimal draftAmount;

  @Column(length = 3)
  private String draftCurrency;

  @Enumerated(EnumType.STRING)
  @Column(length = 32)
  private ExpenseCategory draftCategory;

  private Double draftConfidence;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

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

  public UUID getId() {
    return id;
  }

  public void setId(UUID id) {
    this.id = id;
  }

  public String getObjectKey() {
    return objectKey;
  }

  public void setObjectKey(String objectKey) {
    this.objectKey = objectKey;
  }

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public long getSizeBytes() {
    return sizeBytes;
  }

  public void setSizeBytes(long sizeBytes) {
    this.sizeBytes = sizeBytes;
  }

  public UploadStatus getStatus() {
    return status;
  }

  public void setStatus(UploadStatus status) {
    this.status = status;
    this.updatedAt = Instant.now();
  }

  public String getRawExtractJson() {
    return rawExtractJson;
  }

  public void setRawExtractJson(String rawExtractJson) {
    this.rawExtractJson = rawExtractJson;
  }

  public String getDraftMerchant() {
    return draftMerchant;
  }

  public void setDraftMerchant(String draftMerchant) {
    this.draftMerchant = draftMerchant;
  }

  public java.time.LocalDate getDraftExpenseDate() {
    return draftExpenseDate;
  }

  public void setDraftExpenseDate(java.time.LocalDate draftExpenseDate) {
    this.draftExpenseDate = draftExpenseDate;
  }

  public java.math.BigDecimal getDraftAmount() {
    return draftAmount;
  }

  public void setDraftAmount(java.math.BigDecimal draftAmount) {
    this.draftAmount = draftAmount;
  }

  public String getDraftCurrency() {
    return draftCurrency;
  }

  public void setDraftCurrency(String draftCurrency) {
    this.draftCurrency = draftCurrency;
  }

  public ExpenseCategory getDraftCategory() {
    return draftCategory;
  }

  public void setDraftCategory(ExpenseCategory draftCategory) {
    this.draftCategory = draftCategory;
  }

  public Double getDraftConfidence() {
    return draftConfidence;
  }

  public void setDraftConfidence(Double draftConfidence) {
    this.draftConfidence = draftConfidence;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getUpdatedAt() {
    return updatedAt;
  }
}
