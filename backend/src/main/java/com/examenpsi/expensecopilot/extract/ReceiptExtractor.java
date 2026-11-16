package com.examenpsi.expensecopilot.extract;

import com.examenpsi.expensecopilot.web.dto.ExtractDraftDto;

public interface ReceiptExtractor {
  ExtractDraftDto extract(byte[] imageBytes, String contentType);
}
