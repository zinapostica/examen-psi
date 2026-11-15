package com.examenpsi.expensecopilot.service;

import com.examenpsi.expensecopilot.config.AppProperties;
import com.examenpsi.expensecopilot.domain.ReceiptUpload;
import com.examenpsi.expensecopilot.domain.UploadStatus;
import com.examenpsi.expensecopilot.extract.ReceiptExtractor;
import com.examenpsi.expensecopilot.repository.ReceiptUploadRepository;
import com.examenpsi.expensecopilot.storage.ObjectStorage;
import com.examenpsi.expensecopilot.storage.StoredObject;
import com.examenpsi.expensecopilot.web.dto.ExtractDraftDto;
import com.examenpsi.expensecopilot.web.dto.ReceiptUploadResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ReceiptService {

  private static final Logger log = LoggerFactory.getLogger(ReceiptService.class);
  private static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

  private final ReceiptUploadRepository uploadRepository;
  private final ObjectStorage objectStorage;
  private final ReceiptExtractor receiptExtractor;
  private final AppProperties props;
  private final ObjectMapper objectMapper;

  public ReceiptService(
      ReceiptUploadRepository uploadRepository,
      ObjectStorage objectStorage,
      ReceiptExtractor receiptExtractor,
      AppProperties props,
      ObjectMapper objectMapper
  ) {
    this.uploadRepository = uploadRepository;
    this.objectStorage = objectStorage;
    this.receiptExtractor = receiptExtractor;
    this.props = props;
    this.objectMapper = objectMapper;
  }

  @Transactional
  public ReceiptUploadResponse upload(MultipartFile file) {
    validateFile(file);
    byte[] bytes;
    try {
      bytes = file.getBytes();
    } catch (IOException e) {
      throw new IllegalStateException("Failed to read upload", e);
    }

    String extension = extensionFor(file.getContentType());
    UUID id = UUID.randomUUID();
    String objectKey = "receipts/" + id + extension;
    StoredObject stored = objectStorage.store(objectKey, file.getContentType(), bytes);

    ReceiptUpload upload = new ReceiptUpload();
    upload.setId(id);
    upload.setObjectKey(stored.objectKey());
    upload.setContentType(file.getContentType());
    upload.setSizeBytes(bytes.length);
    upload.setStatus(UploadStatus.UPLOADED);
    uploadRepository.save(upload);

    log.info("Receipt uploaded id={} sizeBytes={} storage={}", id, bytes.length, props.storage().type());
    return toResponse(upload);
  }

  @Transactional
  public ReceiptUploadResponse extract(UUID id) {
    ReceiptUpload upload = uploadRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Receipt upload not found: " + id));

    if (upload.getStatus() == UploadStatus.CONFIRMED) {
      return toResponse(upload);
    }

    upload.setStatus(UploadStatus.EXTRACTING);
    uploadRepository.save(upload);

    try (InputStream in = objectStorage.open(upload.getObjectKey())) {
      byte[] bytes = in.readAllBytes();
      long start = System.currentTimeMillis();
      ExtractDraftDto draft = receiptExtractor.extract(bytes, upload.getContentType());
      log.info("Extract finished uploadId={} latencyMs={} extractor={}",
          id, System.currentTimeMillis() - start, props.extractor().type());

      upload.setDraftMerchant(draft.merchant());
      upload.setDraftExpenseDate(draft.expenseDate());
      upload.setDraftAmount(draft.amount());
      upload.setDraftCurrency(draft.currency() == null ? props.currency() : draft.currency());
      upload.setDraftCategory(draft.category());
      upload.setDraftConfidence(draft.confidence());
      upload.setRawExtractJson(objectMapper.writeValueAsString(draft));
      upload.setStatus(UploadStatus.EXTRACTED);
      uploadRepository.save(upload);
      return toResponse(upload);
    } catch (Exception e) {
      upload.setStatus(UploadStatus.FAILED);
      uploadRepository.save(upload);
      throw new IllegalStateException("Extract failed: " + e.getMessage(), e);
    }
  }

  @Transactional(readOnly = true)
  public ReceiptUploadResponse get(UUID id) {
    ReceiptUpload upload = uploadRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Receipt upload not found: " + id));
    return toResponse(upload);
  }

  public ReceiptUpload requireUpload(UUID id) {
    return uploadRepository.findById(id)
        .orElseThrow(() -> new NoSuchElementException("Receipt upload not found: " + id));
  }

  public void markConfirmed(ReceiptUpload upload) {
    upload.setStatus(UploadStatus.CONFIRMED);
    uploadRepository.save(upload);
  }

  private ReceiptUploadResponse toResponse(ReceiptUpload upload) {
    return new ReceiptUploadResponse(
        upload.getId(),
        upload.getStatus(),
        upload.getContentType(),
        upload.getSizeBytes(),
        objectStorage.resolveUrl(upload.getObjectKey()),
        upload.getDraftMerchant(),
        upload.getDraftExpenseDate(),
        upload.getDraftAmount(),
        upload.getDraftCurrency(),
        upload.getDraftCategory(),
        upload.getDraftConfidence(),
        upload.getRawExtractJson(),
        upload.getCreatedAt()
    );
  }

  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File is required");
    }
    if (file.getSize() > props.maxUploadBytes()) {
      throw new IllegalArgumentException("File exceeds max size of " + props.maxUploadBytes() + " bytes");
    }
    String contentType = file.getContentType();
    if (contentType == null || !ALLOWED_TYPES.contains(contentType.toLowerCase())) {
      throw new IllegalArgumentException("Only JPEG, PNG, and WebP images are allowed");
    }
    try {
      byte[] header = file.getInputStream().readNBytes(12);
      if (!looksLikeImage(header, contentType)) {
        throw new IllegalArgumentException("File content does not match an allowed image type");
      }
    } catch (IOException e) {
      throw new IllegalArgumentException("Unable to validate file content");
    }
  }

  private static boolean looksLikeImage(byte[] header, String contentType) {
    if (header.length < 3) {
      return false;
    }
    String ct = contentType.toLowerCase();
    if (ct.equals("image/jpeg")) {
      return (header[0] & 0xFF) == 0xFF && (header[1] & 0xFF) == 0xD8;
    }
    if (ct.equals("image/png")) {
      return header[0] == (byte) 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47;
    }
    if (ct.equals("image/webp")) {
      return header.length >= 12
          && header[0] == 'R' && header[1] == 'I' && header[2] == 'F' && header[3] == 'F'
          && header[8] == 'W' && header[9] == 'E' && header[10] == 'B' && header[11] == 'P';
    }
    return false;
  }

  private static String extensionFor(String contentType) {
    return switch (contentType.toLowerCase()) {
      case "image/png" -> ".png";
      case "image/webp" -> ".webp";
      default -> ".jpg";
    };
  }
}
