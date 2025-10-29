package com.umdev.infoeste.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductPublicDto(
        UUID id,
        String name,
        BigDecimal price,
        Integer stock,
        String category,
        String storeName,
        String imageUrl
) {
}