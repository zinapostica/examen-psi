package com.examenpsi.expensecopilot.storage;

import com.examenpsi.expensecopilot.config.AppProperties;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalObjectStorage implements ObjectStorage {

  private final Path root;
  private final String publicBaseUrl;

  public LocalObjectStorage(AppProperties props) throws IOException {
    this.root = Path.of(props.storage().localDir()).toAbsolutePath().normalize();
    Files.createDirectories(this.root);
    String base = props.storage().publicBaseUrl();
    this.publicBaseUrl = base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
  }

  @Override
  public StoredObject store(String suggestedKey, String contentType, byte[] bytes) {
    try {
      Path target = root.resolve(suggestedKey).normalize();
      if (!target.startsWith(root)) {
        throw new IllegalArgumentException("Invalid object key");
      }
      Files.createDirectories(target.getParent());
      Files.write(target, bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
      return new StoredObject(suggestedKey, resolveUrl(suggestedKey));
    } catch (IOException e) {
      throw new IllegalStateException("Failed to store file locally: " + e.getMessage(), e);
    }
  }

  @Override
  public InputStream open(String objectKey) {
    try {
      Path target = root.resolve(objectKey).normalize();
      if (!target.startsWith(root) || !Files.exists(target)) {
        throw new IllegalArgumentException("Object not found: " + objectKey);
      }
      return Files.newInputStream(target);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to open local object", e);
    }
  }

  @Override
  public String resolveUrl(String objectKey) {
    return publicBaseUrl + "/files/" + objectKey;
  }
}
