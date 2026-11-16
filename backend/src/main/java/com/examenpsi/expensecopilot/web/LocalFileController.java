package com.examenpsi.expensecopilot.web;

import com.examenpsi.expensecopilot.storage.ObjectStorage;
import java.io.InputStream;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileController {

  private final ObjectStorage objectStorage;

  public LocalFileController(ObjectStorage objectStorage) {
    this.objectStorage = objectStorage;
  }

  @GetMapping("/files/receipts/{filename}")
  public ResponseEntity<InputStreamResource> get(@PathVariable String filename) {
    String key = "receipts/" + filename;
    InputStream stream = objectStorage.open(key);
    MediaType mediaType = mediaTypeFor(filename);
    return ResponseEntity.ok()
        .contentType(mediaType)
        .body(new InputStreamResource(stream));
  }

  private static MediaType mediaTypeFor(String filename) {
    String lower = filename.toLowerCase();
    if (lower.endsWith(".png")) {
      return MediaType.IMAGE_PNG;
    }
    if (lower.endsWith(".webp")) {
      return MediaType.parseMediaType("image/webp");
    }
    return MediaType.IMAGE_JPEG;
  }
}
