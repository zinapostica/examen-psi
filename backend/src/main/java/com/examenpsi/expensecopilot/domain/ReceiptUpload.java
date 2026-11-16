package com.examenpsi.expensecopilot.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "receipt_uploads")
public class ReceiptUpload {

  @Id
  private UUID id;

  private String objectKey;
  private String contentType;
  private long sizeBytes;
  private UploadStatus status;
  private String rawExtractJson;
  private String draftMerchant;
  private LocalDate draftExpenseDate;
  private BigDecimal draftAmount;
  private String draftCurrency;
  private ExpenseCategory draftCategory;
  private Double draftConfidence;
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
    touch();
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

  public LocalDate getDraftExpenseDate() {
    return draftExpenseDate;
  }

  public void setDraftExpenseDate(LocalDate draftExpenseDate) {
    this.draftExpenseDate = draftExpenseDate;
  }

  public BigDecimal getDraftAmount() {
    return draftAmount;
  }

  public void setDraftAmount(BigDecimal draftAmount) {
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
