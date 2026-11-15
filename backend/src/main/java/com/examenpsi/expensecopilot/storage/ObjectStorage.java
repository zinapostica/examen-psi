package com.examenpsi.expensecopilot.storage;

import java.io.InputStream;

public interface ObjectStorage {
  StoredObject store(String suggestedKey, String contentType, byte[] bytes);

  InputStream open(String objectKey);

  String resolveUrl(String objectKey);
}
