package com.examenpsi.expensecopilot.repository;

import com.examenpsi.expensecopilot.domain.ReceiptUpload;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceiptUploadRepository extends JpaRepository<ReceiptUpload, UUID> {}
