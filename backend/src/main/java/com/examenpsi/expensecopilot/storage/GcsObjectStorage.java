package com.examenpsi.expensecopilot.storage;

import com.examenpsi.expensecopilot.config.AppProperties;
import java.io.InputStream;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Optional GCS stub — activate with STORAGE=gcs after adding the GCS client dependency
 * and Application Default Credentials / service account. For MVP demos prefer local or Cloudinary.
 */
@Component
@ConditionalOnProperty(name = "app.storage.type", havingValue = "gcs")
public class GcsObjectStorage implements ObjectStorage {

  public GcsObjectStorage(AppProperties props) {
    throw new IllegalStateException(
        "STORAGE=gcs is scaffolded for Cloud Run/GCS exam deploy. "
            + "Add google-cloud-storage dependency and wire the client, "
            + "or use STORAGE=local / STORAGE=cloudinary for free-tier demos. "
            + "Configured bucket=" + props.gcs().bucket());
  }

  @Override
  public StoredObject store(String suggestedKey, String contentType, byte[] bytes) {
    throw new UnsupportedOperationException("GCS storage not wired in this MVP build");
  }

  @Override
  public InputStream open(String objectKey) {
    throw new UnsupportedOperationException("GCS storage not wired in this MVP build");
  }

  @Override
  public String resolveUrl(String objectKey) {
    throw new UnsupportedOperationException("GCS storage not wired in this MVP build");
  }
}
