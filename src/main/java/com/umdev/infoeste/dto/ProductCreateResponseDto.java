package com.umdev.infoeste.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ProductCreateResponseDto(
        UUID id,
        UUID storeId,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        String category,
        String imageUrl,
        LocalDateTime createdAt
) {
}