package com.examenpsi.expensecopilot.storage;

import com.examenpsi.expensecopilot.config.AppProperties;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Cloudinary-ready image storage via signed REST upload.
 * Requires CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, CLOUDINARY_API_SECRET.
 */
@Component
@ConditionalOnProperty(name = "app.storage.type", havingValue = "cloudinary")
public class CloudinaryObjectStorage implements ObjectStorage {

  private final AppProperties.Cloudinary cloudinary;
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient = HttpClient.newHttpClient();
  private final Map<String, String> urlByKey = new ConcurrentHashMap<>();

  public CloudinaryObjectStorage(AppProperties props, ObjectMapper objectMapper) {
    this.cloudinary = props.cloudinary();
    this.objectMapper = objectMapper;
    if (isBlank(cloudinary.cloudName()) || isBlank(cloudinary.apiKey()) || isBlank(cloudinary.apiSecret())) {
      throw new IllegalStateException(
          "STORAGE=cloudinary requires CLOUDINARY_CLOUD_NAME, CLOUDINARY_API_KEY, CLOUDINARY_API_SECRET");
    }
  }

  @Override
  public StoredObject store(String suggestedKey, String contentType, byte[] bytes) {
    try {
      String publicId = suggestedKey.replace('.', '_').replace('/', '_');
      long timestamp = Instant.now().getEpochSecond();
      String toSign = "public_id=" + publicId + "&timestamp=" + timestamp + cloudinary.apiSecret();
      String signature = sha1(toSign);

      String boundary = "----ExpenseCopilot" + timestamp;
      byte[] body = buildMultipart(boundary, publicId, timestamp, signature, contentType, bytes);

      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create("https://api.cloudinary.com/v1_1/" + cloudinary.cloudName() + "/image/upload"))
          .header("Content-Type", "multipart/form-data; boundary=" + boundary)
          .POST(HttpRequest.BodyPublishers.ofByteArray(body))
          .build();

      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() >= 300) {
        throw new IllegalStateException("Cloudinary upload failed: " + response.body());
      }
      JsonNode json = objectMapper.readTree(response.body());
      String url = json.path("secure_url").asText();
      urlByKey.put(suggestedKey, url);
      return new StoredObject(suggestedKey, url);
    } catch (IOException e) {
      throw new IllegalStateException("Cloudinary upload error: " + e.getMessage(), e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Cloudinary upload interrupted", e);
    }
  }

  @Override
  public InputStream open(String objectKey) {
    String url = resolveUrl(objectKey);
    try {
      HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();
      HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
      if (response.statusCode() >= 300) {
        throw new IllegalStateException("Failed to download Cloudinary object");
      }
      return new ByteArrayInputStream(response.body());
    } catch (IOException e) {
      throw new IllegalStateException("Cloudinary download error", e);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Cloudinary download interrupted", e);
    }
  }

  @Override
  public String resolveUrl(String objectKey) {
    String cached = urlByKey.get(objectKey);
    if (cached != null) {
      return cached;
    }
    String publicId = objectKey.replace('.', '_').replace('/', '_');
    return "https://res.cloudinary.com/" + cloudinary.cloudName() + "/image/upload/" + publicId;
  }

  private byte[] buildMultipart(
      String boundary,
      String publicId,
      long timestamp,
      String signature,
      String contentType,
      byte[] bytes
  ) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    writePart(baos, boundary, "file", "receipt", contentType, bytes);
    writeTextPart(baos, boundary, "api_key", cloudinary.apiKey());
    writeTextPart(baos, boundary, "timestamp", String.valueOf(timestamp));
    writeTextPart(baos, boundary, "public_id", publicId);
    writeTextPart(baos, boundary, "signature", signature);
    baos.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
    return baos.toByteArray();
  }

  private static void writePart(
      ByteArrayOutputStream baos,
      String boundary,
      String name,
      String filename,
      String contentType,
      byte[] data
  ) throws IOException {
    baos.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
    baos.write(("Content-Disposition: form-data; name=\"" + name + "\"; filename=\"" + filename + "\"\r\n")
        .getBytes(StandardCharsets.UTF_8));
    baos.write(("Content-Type: " + contentType + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
    baos.write(data);
    baos.write("\r\n".getBytes(StandardCharsets.UTF_8));
  }

  private static void writeTextPart(
      ByteArrayOutputStream baos,
      String boundary,
      String name,
      String value
  ) throws IOException {
    baos.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
    baos.write(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n").getBytes(StandardCharsets.UTF_8));
    baos.write(value.getBytes(StandardCharsets.UTF_8));
    baos.write("\r\n".getBytes(StandardCharsets.UTF_8));
  }

  private static String sha1(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-1");
      return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalStateException("SHA-1 unavailable", e);
    }
  }

  private static boolean isBlank(String s) {
    return s == null || s.isBlank();
  }
}
