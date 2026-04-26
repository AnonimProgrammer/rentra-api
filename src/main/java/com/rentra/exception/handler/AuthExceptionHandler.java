package com.rentra.exception.handler;

import java.time.OffsetDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.rentra.dto.error.ErrorResponse;
import com.rentra.exception.ErrorCode;
import com.rentra.exception.InvalidCredentialsException;

@RestControllerAdvice
public class AuthExceptionHandler {
    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex) {
        HttpStatus status = HttpStatus.UNAUTHORIZED;
        return ResponseEntity.status(status)
                .body(new ErrorResponse(
                        status.value(), ErrorCode.UNAUTHORIZED.name(), ex.getMessage(), OffsetDateTime.now()));
    }

    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDenied(AuthorizationDeniedException ex) {
        HttpStatus status = HttpStatus.FORBIDDEN;
        return ResponseEntity.status(status)
                .body(new ErrorResponse(
                        status.value(), ErrorCode.FORBIDDEN.name(), "Access denied.", OffsetDateTime.now()));
    }
}
