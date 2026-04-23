package com.rentra.exception.auth;

public class ResourceNotFoundException extends AuthException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
