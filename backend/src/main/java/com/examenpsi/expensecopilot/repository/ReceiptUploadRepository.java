package com.examenpsi.expensecopilot.repository;

import com.examenpsi.expensecopilot.domain.ReceiptUpload;
import java.util.UUID;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ReceiptUploadRepository extends MongoRepository<ReceiptUpload, UUID> {}
