package com.rentra.dto.error;

import java.time.OffsetDateTime;
import java.util.Map;

public record ValidationErrorResponse(
        Integer status, String error, Map<String, String> errors, OffsetDateTime timestamp) {}
