package com.umdev.infoeste.dto;

import java.util.UUID;

public record ProductImageUpdateResponseDto(
        UUID id,
        String imageUrl
) {
}