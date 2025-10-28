package com.umdev.infoeste.mappers;

import com.umdev.infoeste.dto.*;
import com.umdev.infoeste.entities.Store;
import com.umdev.infoeste.entities.UserRole;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDateTime;

@Mapper(componentModel = "spring")
public interface StoreMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "role", expression = "java(com.umdev.infoeste.entities.UserRole.USER)")
    @Mapping(target = "products", ignore = true)
    Store toEntity(StoreRegisterDto dto);

    StoreRegisterResponseDto toRegisterResponse(Store store);

    StoreLoginResponseDto.StoreInfo toLoginStoreInfo(Store store);

    StoreProfileDto toProfileDto(Store store);

    StorePublicDto toPublicDto(Store store);

    StoreDetailDto toDetailDto(Store store);
}