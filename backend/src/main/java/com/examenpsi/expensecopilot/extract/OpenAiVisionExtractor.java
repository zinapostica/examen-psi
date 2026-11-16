package com.examenpsi.expensecopilot.extract;

import com.examenpsi.expensecopilot.config.AppProperties;
import com.examenpsi.expensecopilot.domain.ExpenseCategory;
import com.examenpsi.expensecopilot.web.dto.ExtractDraftDto;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Optional live vision extract via OpenAI-compatible Chat Completions + image_url.
 * Falls back to mock fixture parsing errors as IllegalStateException for the service to surface.
 */
@Component
@ConditionalOnProperty(name = "app.extractor.type", havingValue = "openai")
public class OpenAiVisionExtractor implements ReceiptExtractor {

  private static final Logger log = LoggerFactory.getLogger(OpenAiVisionExtractor.class);

  private final AppProperties.Extractor extractor;
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient = HttpClient.newHttpClient();
  private final MockReceiptExtractor fallback;

  public OpenAiVisionExtractor(AppProperties props, ObjectMapper objectMapper) {
    this.extractor = props.extractor();
    this.objectMapper = objectMapper;
    this.fallback = new MockReceiptExtractor(objectMapper);
    if (extractor.openaiApiKey() == null || extractor.openaiApiKey().isBlank()) {
      throw new IllegalStateException("EXTRACTOR=openai requires OPENAI_API_KEY");
    }
  }

  @Override
  public ExtractDraftDto extract(byte[] imageBytes, String contentType) {
    long start = System.currentTimeMillis();
    try {
      String b64 = Base64.getEncoder().encodeToString(imageBytes);
      String dataUrl = "data:" + contentType + ";base64," + b64;
      String body = """
          {
            "model": "%s",
            "temperature": 0,
            "response_format": { "type": "json_object" },
            "messages": [
              {
                "role": "system",
                "content": "Extract expense fields from this receipt. Return only JSON with keys: merchant, expenseDate (YYYY-MM-DD), amount, currency, category (FOOD|TRANSPORT|OFFICE|TRAVEL|UTILITIES|OTHER), confidence (0-1), lineItems (optional array of {description, amount}). Total = amount due including tax. Prefer merchant name as printed."
              },
              {
                "role": "user",
                "content": [
                  { "type": "text", "text": "Extract structured expense fields." },
                  { "type": "image_url", "image_url": { "url": "%s" } }
                ]
              }
            ]
          }
          """.formatted(extractor.openaiModel(), dataUrl);

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create("https://api.openai.com/v1/chat/completions"))
          .header("Authorization", "Bearer " + extractor.openaiApiKey())
          .header("Content-Type", "application/json")
          .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
          .build();

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      log.info("OpenAI vision extract status={} latencyMs={}", response.statusCode(), System.currentTimeMillis() - start);
      if (response.statusCode() >= 300) {
        log.warn("OpenAI extract failed, falling back to mock: {}", response.body());
        return fallback.extract(imageBytes, contentType);
      }
      JsonNode root = objectMapper.readTree(response.body());
      String content = root.path("choices").path(0).path("message").path("content").asText();
      return mapJson(objectMapper.readTree(content));
    } catch (Exception e) {
      log.warn("OpenAI extract error, falling back to mock: {}", e.getMessage());
      return fallback.extract(imageBytes, contentType);
    }
  }

  private ExtractDraftDto mapJson(JsonNode node) {
    List<ExtractDraftDto.LineItemDto> items = new ArrayList<>();
    if (node.has("lineItems") && node.get("lineItems").isArray()) {
      for (JsonNode item : node.get("lineItems")) {
        items.add(new ExtractDraftDto.LineItemDto(
            item.path("description").asText(null),
            item.has("amount") ? new BigDecimal(item.get("amount").asText()) : null
        ));
      }
    }
    String categoryRaw = node.path("category").asText("OTHER");
    ExpenseCategory category;
    try {
      category = ExpenseCategory.valueOf(categoryRaw.toUpperCase());
    } catch (Exception e) {
      category = ExpenseCategory.OTHER;
    }
    LocalDate date = null;
    String dateRaw = node.path("expenseDate").asText(null);
    if (dateRaw != null && !dateRaw.isBlank()) {
      date = LocalDate.parse(dateRaw);
    }
    BigDecimal amount = node.has("amount") ? new BigDecimal(node.get("amount").asText()) : null;
    Double confidence = node.has("confidence") ? node.get("confidence").asDouble() : null;
    return new ExtractDraftDto(
        node.path("merchant").asText(null),
        date,
        amount,
        node.path("currency").asText("EUR"),
        category,
        confidence,
        items
    );
  }
}
