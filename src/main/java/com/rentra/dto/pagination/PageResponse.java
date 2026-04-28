package com.rentra.dto.pagination;

import java.util.List;

public record PageResponse<T>(List<T> data, PaginationMeta pagination) {}
