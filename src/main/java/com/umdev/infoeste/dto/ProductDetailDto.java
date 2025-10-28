package com.umdev.infoeste.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductDetailDto(
        UUID id,
        String name,
        String description,
        BigDecimal price,
        Integer stock,
        String category,
        String imageUrl,
        StoreInfo store
) {
    public record StoreInfo(
            UUID id,
            String name
    ) {
    }
}