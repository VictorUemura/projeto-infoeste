package com.umdev.infoeste.dto;

import java.util.UUID;

public record StoreLoginResponseDto(
        String token,
        StoreInfo store
) {
    public record StoreInfo(
            UUID id,
            String name,
            String email
    ) {
    }
}