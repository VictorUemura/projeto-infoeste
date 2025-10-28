package com.umdev.infoeste.controllers;

import com.umdev.infoeste.dto.*;
import com.umdev.infoeste.services.ProductService;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/products")
@Validated
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<ProductCreateResponseDto> createProduct(
            Authentication authentication,
            @RequestParam("name") @NotBlank(message = "Name is required") String name,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("price") @NotNull(message = "Price is required") 
            @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0") BigDecimal price,
            @RequestParam("stock") @NotNull(message = "Stock is required") 
            @Min(value = 0, message = "Stock must be 0 or greater") Integer stock,
            @RequestParam(value = "category", required = false) String category,
            @RequestPart("file") MultipartFile file) {
        ProductCreateDto productDto = new ProductCreateDto(name, description, price, stock, category);
        
        String storeEmail = authentication.getName();
        ProductCreateResponseDto response = productService.createProduct(storeEmail, productDto, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<ProductMyListDto>> getMyProducts(Authentication authentication) {
        String storeEmail = authentication.getName();
        List<ProductMyListDto> products = productService.getMyProducts(storeEmail);
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ProductCreateResponseDto> updateProduct(
            Authentication authentication,
            @PathVariable UUID productId,
            @RequestBody ProductUpdateDto updateDto) {
        
        String storeEmail = authentication.getName();
        ProductCreateResponseDto response = productService.updateProduct(storeEmail, productId, updateDto);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{productId}/image", consumes = {"multipart/form-data"})
    public ResponseEntity<ProductImageUpdateResponseDto> updateProductImage(
            Authentication authentication,
            @PathVariable UUID productId,
            @RequestPart("file") MultipartFile file) {
        
        String storeEmail = authentication.getName();
        ProductImageUpdateResponseDto response = productService.updateProductImage(storeEmail, productId, file);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<PaginatedResponseDto<ProductPublicDto>> getProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {
        
        PaginatedResponseDto<ProductPublicDto> response = productService.getProducts(
                page, limit, q, category, minPrice, maxPrice);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDetailDto> getProductById(@PathVariable UUID productId) {
        ProductDetailDto product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(
            Authentication authentication,
            @PathVariable UUID productId) {
        
        String storeEmail = authentication.getName();
        productService.deleteProduct(storeEmail, productId);
        return ResponseEntity.noContent().build();
    }
}