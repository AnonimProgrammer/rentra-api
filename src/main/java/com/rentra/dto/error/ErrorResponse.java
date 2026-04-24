package com.rentra.dto.error;

import java.time.OffsetDateTime;

public record ErrorResponse(Integer status, String error, String message, OffsetDateTime timestamp) {}
