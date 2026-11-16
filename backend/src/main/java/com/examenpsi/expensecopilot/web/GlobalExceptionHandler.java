package com.examenpsi.expensecopilot.web;

import com.examenpsi.expensecopilot.web.dto.ApiError;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
    Map<String, String> fields = new LinkedHashMap<>();
    for (FieldError error : ex.getBindingResult().getFieldErrors()) {
      fields.put(error.getField(), error.getDefaultMessage());
    }
    return ResponseEntity.badRequest().body(new ApiError(
        Instant.now(),
        400,
        "Validation failed",
        "Request validation failed",
        fields
    ));
  }

  @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
  ResponseEntity<ApiError> handleBadRequest(RuntimeException ex) {
    return ResponseEntity.badRequest().body(new ApiError(
        Instant.now(),
        400,
        "Bad request",
        ex.getMessage(),
        Map.of()
    ));
  }

  @ExceptionHandler(NoSuchElementException.class)
  ResponseEntity<ApiError> handleNotFound(NoSuchElementException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiError(
        Instant.now(),
        404,
        "Not found",
        ex.getMessage(),
        Map.of()
    ));
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  ResponseEntity<ApiError> handleTooLarge(MaxUploadSizeExceededException ex) {
    return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(new ApiError(
        Instant.now(),
        413,
        "Payload too large",
        "File exceeds max upload size (5 MB)",
        Map.of()
    ));
  }

  @ExceptionHandler(Exception.class)
  ResponseEntity<ApiError> handleGeneric(Exception ex) {
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiError(
        Instant.now(),
        500,
        "Internal error",
        ex.getMessage() == null ? "Unexpected error" : ex.getMessage(),
        Map.of()
    ));
  }
}
