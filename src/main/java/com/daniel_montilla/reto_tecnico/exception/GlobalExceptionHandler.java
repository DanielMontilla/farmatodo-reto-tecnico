package com.daniel_montilla.reto_tecnico.exception;

import com.daniel_montilla.reto_tecnico.dto.ErrorResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex,
      WebRequest request) {
    // Create the base error response
    ErrorResponse errorResponse = new ErrorResponse(
        LocalDateTime.now(),
        HttpStatus.BAD_REQUEST.value(),
        "Bad Request",
        "Input validation failed",
        request.getDescription(false).replace("uri=", ""));

    // Construct the details map and set it
    Map<String, String> validationErrors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach((error) -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      validationErrors.put(fieldName, errorMessage);
    });
    errorResponse.setDetails(validationErrors);

    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(TokenizationRejectedException.class)
  public ResponseEntity<ErrorResponse> handleTokenizationRejected(TokenizationRejectedException ex,
      WebRequest request) {

    logger.error("Validation error on request to {}: {}", request.getDescription(false), ex.getMessage());

    ErrorResponse errorResponse = new ErrorResponse(
        LocalDateTime.now(),
        HttpStatus.UNPROCESSABLE_ENTITY.value(),
        "Unprocessable Entity",
        ex.getMessage(),
        request.getDescription(false).replace("uri=", ""));
    return new ResponseEntity<>(errorResponse, HttpStatus.UNPROCESSABLE_ENTITY);
  }

  @ExceptionHandler(TokenGenerationException.class)
  public ResponseEntity<ErrorResponse> handleTokenGenerationException(TokenGenerationException ex, WebRequest request) {
    logger.error("Tokenization rejected for request to {}: {}", request.getDescription(false), ex.getMessage());

    ErrorResponse errorResponse = new ErrorResponse(
        LocalDateTime.now(),
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "Internal Server Error",
        ex.getMessage(),
        request.getDescription(false).replace("uri=", ""));
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex,
      WebRequest request) {

    logger.error("Data integrity violation on request to {}: {}", request.getDescription(false),
        ex.getMostSpecificCause().getMessage());

    ErrorResponse errorResponse = new ErrorResponse(
        LocalDateTime.now(),
        HttpStatus.BAD_REQUEST.value(),
        "Bad Request",
        "A unique constraint violation occurred. Please check that all required fields have unique values.",
        request.getDescription(false).replace("uri=", ""));
    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
  }

  // Fallback handler
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {

    String errorId = UUID.randomUUID().toString();
    String path = request.getDescription(false).replace("uri=", "");

    logger.error("Error ID: {} | Path: {} | Exception: {} - {}",
        errorId,
        path,
        ex.getClass().getName(),
        ex.getMessage());

    logger.debug("Stack trace for Error ID: {}", errorId, ex);

    String userMessage = String.format("An unexpected error occurred. Please report this ID to support: %s", errorId);

    ErrorResponse errorResponse = new ErrorResponse(
        LocalDateTime.now(),
        HttpStatus.INTERNAL_SERVER_ERROR.value(),
        "Internal Server Error",
        userMessage,
        request.getDescription(false).replace("uri=", ""));
    return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
}