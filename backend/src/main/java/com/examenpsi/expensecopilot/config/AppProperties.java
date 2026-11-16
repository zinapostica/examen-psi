package com.examenpsi.expensecopilot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
    Cors cors,
    Storage storage,
    Cloudinary cloudinary,
    Gcs gcs,
    Extractor extractor,
    String currency,
    long maxUploadBytes
) {
  public record Cors(String allowedOrigins) {}

  public record Storage(String type, String localDir, String publicBaseUrl) {}

  public record Cloudinary(String cloudName, String apiKey, String apiSecret) {}

  public record Gcs(String bucket, String projectId) {}

  public record Extractor(String type, String openaiApiKey, String openaiModel) {}
}
