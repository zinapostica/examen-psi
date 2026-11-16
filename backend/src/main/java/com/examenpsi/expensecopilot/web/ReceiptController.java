package com.examenpsi.expensecopilot.web;

import com.examenpsi.expensecopilot.service.ReceiptService;
import com.examenpsi.expensecopilot.web.dto.ReceiptUploadResponse;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/receipts")
public class ReceiptController {

  private final ReceiptService receiptService;

  public ReceiptController(ReceiptService receiptService) {
    this.receiptService = receiptService;
  }

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ReceiptUploadResponse upload(@RequestPart("file") MultipartFile file) {
    return receiptService.upload(file);
  }

  @PostMapping("/{id}/extract")
  public ReceiptUploadResponse extract(@PathVariable UUID id) {
    return receiptService.extract(id);
  }

  @GetMapping("/{id}")
  public ReceiptUploadResponse get(@PathVariable UUID id) {
    return receiptService.get(id);
  }
}
