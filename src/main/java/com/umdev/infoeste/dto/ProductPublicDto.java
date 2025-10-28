package com.umdev.infoeste.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductPublicDto(
        UUID id,
        String name,
        BigDecimal price,
        String storeName,
        String imageUrl
) {
}