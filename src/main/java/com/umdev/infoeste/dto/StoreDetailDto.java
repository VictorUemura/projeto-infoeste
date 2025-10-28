package com.umdev.infoeste.dto;

import java.util.UUID;

public record StoreDetailDto(
        UUID id,
        String name,
        String city,
        String address,
        String phone,
        String description
) {
}