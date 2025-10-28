package com.umdev.infoeste.mappers;

import com.umdev.infoeste.dto.*;
import com.umdev.infoeste.entities.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "store", ignore = true)
    @Mapping(target = "imageBase64", ignore = true)
    Product toEntity(ProductCreateDto dto);

    @Mapping(target = "storeId", source = "store.id")
    @Mapping(target = "imageUrl", expression = "java(\"data:image/jpeg;base64,\" + product.getImageBase64())")
    ProductCreateResponseDto toCreateResponse(Product product);

    @Mapping(target = "imageUrl", expression = "java(\"data:image/jpeg;base64,\" + product.getImageBase64())")
    ProductMyListDto toMyListDto(Product product);

    @Mapping(target = "storeName", source = "store.name")
    @Mapping(target = "imageUrl", expression = "java(\"data:image/jpeg;base64,\" + product.getImageBase64())")
    ProductPublicDto toPublicDto(Product product);

    @Mapping(target = "imageUrl", expression = "java(\"data:image/jpeg;base64,\" + product.getImageBase64())")
    @Mapping(target = "store.id", source = "store.id")
    @Mapping(target = "store.name", source = "store.name")
    ProductDetailDto toDetailDto(Product product);

    @Mapping(target = "imageUrl", expression = "java(\"data:image/jpeg;base64,\" + product.getImageBase64())")
    ProductImageUpdateResponseDto toImageUpdateResponse(Product product);
}