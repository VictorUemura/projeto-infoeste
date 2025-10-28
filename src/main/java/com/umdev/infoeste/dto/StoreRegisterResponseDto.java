package com.umdev.infoeste.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record StoreRegisterResponseDto(
        UUID id,
        String name,
        String email,
        String city,
        LocalDateTime createdAt
) {
}