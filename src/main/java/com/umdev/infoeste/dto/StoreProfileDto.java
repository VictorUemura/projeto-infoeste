package com.umdev.infoeste.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record StoreProfileDto(
        UUID id,
        String name,
        String email,
        String description,
        String city,
        LocalDateTime createdAt
) {
}