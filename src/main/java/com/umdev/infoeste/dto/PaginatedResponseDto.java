package com.umdev.infoeste.dto;

import java.util.List;

public record PaginatedResponseDto<T>(
        MetaData meta,
        List<T> data
) {
    public record MetaData(
            int page,
            int limit,
            long total
    ) {
    }
}