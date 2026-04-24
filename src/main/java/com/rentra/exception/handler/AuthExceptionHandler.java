package com.rentra.exception.handler;

import java.time.OffsetDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.rentra.dto.error.ErrorResponse;
import com.rentra.exception.InvalidCredentialsException;

@RestControllerAdvice
public class AuthExceptionHandler {
    private static final String UNAUTHORIZED = "UNAUTHORIZED";

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), UNAUTHORIZED, ex.getMessage(), OffsetDateTime.now()));
    }
}
