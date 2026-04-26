package com.rentra.exception.handler;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.rentra.dto.error.ErrorResponse;
import com.rentra.dto.error.ValidationErrorResponse;
import com.rentra.exception.ConflictException;
import com.rentra.exception.ResourceNotFoundException;
import com.rentra.exception.UnsupportedOperationException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final String INTERNAL_SERVER_ERROR = "INTERNAL_SERVER_ERROR";
    private static final String NOT_FOUND = "NOT_FOUND";
    private static final String BAD_REQUEST = "BAD_REQUEST";
    private static final String CONFLICT = "CONFLICT";
    private static final String VALIDATION_ERROR = "VALIDATION_ERROR";

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity.status(status)
                .body(new ErrorResponse(
                        status.value(), INTERNAL_SERVER_ERROR, "An unexpected error occurred.", OffsetDateTime.now()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        HttpStatus status = HttpStatus.NOT_FOUND;
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), NOT_FOUND, ex.getMessage(), OffsetDateTime.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fieldError -> Optional.ofNullable(fieldError.getDefaultMessage())
                                .orElse("Validation error"),
                        (existing, replacement) -> existing));

        return ResponseEntity.status(status)
                .body(new ValidationErrorResponse(status.value(), VALIDATION_ERROR, errors, OffsetDateTime.now()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), BAD_REQUEST, ex.getMessage(), OffsetDateTime.now()));
    }

    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedOperation(UnsupportedOperationException ex) {
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), BAD_REQUEST, ex.getMessage(), OffsetDateTime.now()));
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        HttpStatus status = HttpStatus.CONFLICT;
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), CONFLICT, ex.getMessage(), OffsetDateTime.now()));
    }
}
