package com.examenpsi.expensecopilot.extract;

import com.examenpsi.expensecopilot.domain.ExpenseCategory;
import com.examenpsi.expensecopilot.web.dto.ExtractDraftDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.extractor.type", havingValue = "mock", matchIfMissing = true)
public class MockReceiptExtractor implements ReceiptExtractor {

  private static final Logger log = LoggerFactory.getLogger(MockReceiptExtractor.class);

  private final ObjectMapper objectMapper;

  public MockReceiptExtractor(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public ExtractDraftDto extract(byte[] imageBytes, String contentType) {
    log.info("Mock extract: bytes={}, contentType={}", imageBytes == null ? 0 : imageBytes.length, contentType);
    try (InputStream in = new ClassPathResource("fixtures/mock-extract.json").getInputStream()) {
      ExtractDraftDto fromFixture = objectMapper.readValue(in, ExtractDraftDto.class);
      if (fromFixture.expenseDate() == null) {
        return withToday(fromFixture);
      }
      return fromFixture;
    } catch (Exception e) {
      log.warn("Fixture missing or unreadable, using hardcoded mock draft: {}", e.getMessage());
      return new ExtractDraftDto(
          "Café Central",
          LocalDate.now().minusDays(1),
          new BigDecimal("12.50"),
          "EUR",
          ExpenseCategory.FOOD,
          0.86,
          List.of(
              new ExtractDraftDto.LineItemDto("Cappuccino", new BigDecimal("3.50")),
              new ExtractDraftDto.LineItemDto("Croissant", new BigDecimal("2.80")),
              new ExtractDraftDto.LineItemDto("Lunch menu", new BigDecimal("6.20"))
          )
      );
    }
  }

  private ExtractDraftDto withToday(ExtractDraftDto dto) {
    return new ExtractDraftDto(
        dto.merchant(),
        LocalDate.now().minusDays(1),
        dto.amount(),
        dto.currency(),
        dto.category(),
        dto.confidence(),
        dto.lineItems()
    );
  }
}
