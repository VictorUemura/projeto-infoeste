package com.umdev.infoeste.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductMyListDto(
        UUID id,
        String name,
        BigDecimal price,
        Integer stock,
        String imageUrl
) {
}