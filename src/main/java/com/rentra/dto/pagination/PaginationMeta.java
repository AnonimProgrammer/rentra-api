package com.rentra.dto.pagination;

public record PaginationMeta(
        String nextCursor, String previousCursor, boolean hasNext, boolean hasPrevious, int limit) {}
