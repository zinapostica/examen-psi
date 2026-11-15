package com.examenpsi.expensecopilot;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class ExpenseFlowIntegrationTest {

  @TempDir
  static Path tempDir;

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry registry) {
    registry.add("spring.data.mongodb.uri",
        () -> "mongodb://localhost:27017/expense_copilot_test");
    registry.add("app.storage.type", () -> "local");
    registry.add("app.storage.local-dir", () -> tempDir.resolve("uploads").toString());
    registry.add("app.storage.public-base-url", () -> "http://localhost:8080");
    registry.add("app.extractor.type", () -> "mock");
    registry.add("app.cors.allowed-origins", () -> "http://localhost:3000");
  }

  @Autowired
  MockMvc mockMvc;

  @Autowired
  MongoTemplate mongoTemplate;

  @BeforeEach
  void cleanDb() {
    mongoTemplate.getDb().drop();
  }

  @Test
  void uploadExtractConfirmList() throws Exception {
    byte[] jpeg = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xD9};
    MockMultipartFile file = new MockMultipartFile(
        "file", "receipt.jpg", "image/jpeg", jpeg);

    MvcResult uploadResult = mockMvc.perform(multipart("/api/receipts").file(file))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("UPLOADED"))
        .andReturn();

    String uploadId = com.jayway.jsonpath.JsonPath.read(
        uploadResult.getResponse().getContentAsString(), "$.id");

    mockMvc.perform(post("/api/receipts/" + uploadId + "/extract"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("EXTRACTED"))
        .andExpect(jsonPath("$.merchant").value("Café Central"));

    String body = """
        {
          "uploadId": "%s",
          "merchant": "Café Central",
          "expenseDate": "2026-11-14",
          "amount": 12.50,
          "currency": "EUR",
          "category": "FOOD",
          "notes": "Demo"
        }
        """.formatted(uploadId);

    mockMvc.perform(post("/api/expenses")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.merchant").value("Café Central"));

    mockMvc.perform(get("/api/expenses").param("month", "2026-11").param("category", "FOOD"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].merchant").value("Café Central"));
  }
}
