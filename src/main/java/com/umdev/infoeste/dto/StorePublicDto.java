package com.umdev.infoeste.dto;

import java.util.UUID;

public record StorePublicDto(
        UUID id,
        String name,
        String city,
        String description
) {
}