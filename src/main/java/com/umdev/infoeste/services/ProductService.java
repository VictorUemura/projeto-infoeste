package com.umdev.infoeste.services;

import com.umdev.infoeste.dto.*;
import com.umdev.infoeste.entities.Product;
import com.umdev.infoeste.entities.Store;
import com.umdev.infoeste.mappers.ProductMapper;
import com.umdev.infoeste.repositories.ProductRepository;
import com.umdev.infoeste.repositories.StoreRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
public class ProductService {
    
    private final ProductRepository productRepository;
    private final StoreRepository storeRepository;
    private final ProductMapper productMapper;
    private final Logger productLogger = LoggerFactory.getLogger(ProductService.class);
    
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final List<String> ALLOWED_TYPES = List.of("image/jpeg", "image/png", "image/webp");

    public ProductService(ProductRepository productRepository, StoreRepository storeRepository, 
                         ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.storeRepository = storeRepository;
        this.productMapper = productMapper;
    }

    public ProductCreateResponseDto createProduct(String storeEmail, ProductCreateDto productDto, 
                                                MultipartFile file) {
        productLogger.info("Creating product for store: {}", storeEmail);

        validateImageFile(file);

        Store store = storeRepository.findByEmail(storeEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Store not found with email: " + storeEmail));

        Product product = productMapper.toEntity(productDto);
        product.setStore(store);
        product.setCreatedAt(LocalDateTime.now());

        try {
            String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
            product.setImageBase64(base64Image);
        } catch (Exception e) {
            productLogger.error("Error converting image to base64", e);
            throw new RuntimeException("Error processing image file");
        }

        Product savedProduct = productRepository.save(product);
        productLogger.info("Product created successfully with ID: {}", savedProduct.getId());

        return productMapper.toCreateResponse(savedProduct);
    }

    public List<ProductMyListDto> getMyProducts(String storeEmail) {
        productLogger.info("Fetching products for store: {}", storeEmail);
        
        Store store = storeRepository.findByEmail(storeEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Store not found with email: " + storeEmail));

        List<Product> products = productRepository.findByStoreId(store.getId());
        
        return products.stream()
                .map(productMapper::toMyListDto)
                .toList();
    }

    public ProductCreateResponseDto updateProduct(String storeEmail, UUID productId, 
                                                ProductUpdateDto updateDto) {
        productLogger.info("Updating product {} for store: {}", productId, storeEmail);
        
        Store store = storeRepository.findByEmail(storeEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Store not found with email: " + storeEmail));

        Product product = productRepository.findByIdAndStoreId(productId, store.getId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found or doesn't belong to store"));

        // Atualizar campos
        product.setName(updateDto.name());
        product.setPrice(updateDto.price());
        product.setStock(updateDto.stock());
        product.setCategory(updateDto.category());

        Product savedProduct = productRepository.save(product);
        productLogger.info("Product {} updated successfully", productId);

        return productMapper.toCreateResponse(savedProduct);
    }

    public ProductImageUpdateResponseDto updateProductImage(String storeEmail, UUID productId, 
                                                          MultipartFile file) {
        productLogger.info("Updating image for product {} from store: {}", productId, storeEmail);

        validateImageFile(file);
        
        Store store = storeRepository.findByEmail(storeEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Store not found with email: " + storeEmail));

        Product product = productRepository.findByIdAndStoreId(productId, store.getId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found or doesn't belong to store"));

        try {
            String base64Image = Base64.getEncoder().encodeToString(file.getBytes());
            product.setImageBase64(base64Image);
        } catch (Exception e) {
            productLogger.error("Error converting image to base64", e);
            throw new RuntimeException("Error processing image file");
        }

        Product savedProduct = productRepository.save(product);
        productLogger.info("Image updated successfully for product {}", productId);

        return productMapper.toImageUpdateResponse(savedProduct);
    }

    public PaginatedResponseDto<ProductPublicDto> getProducts(int page, int limit, String query, 
                                                            String category, BigDecimal minPrice, 
                                                            BigDecimal maxPrice) {
        productLogger.info("Fetching public products - page: {}, limit: {}, query: {}, category: {}", 
                          page, limit, query, category);
        
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Product> productPage = productRepository.findProductsWithFilters(
                query, category, minPrice, maxPrice, pageable);

        List<ProductPublicDto> productList = productPage.getContent()
                .stream()
                .map(productMapper::toPublicDto)
                .toList();

        PaginatedResponseDto.MetaData meta = new PaginatedResponseDto.MetaData(
                page, 
                limit, 
                productPage.getTotalElements()
        );

        return new PaginatedResponseDto<>(meta, productList);
    }

    public PaginatedResponseDto<ProductPublicDto> getProductsByStore(UUID storeId, int page, int limit, 
                                                                   String query, String category, 
                                                                   BigDecimal minPrice, BigDecimal maxPrice) {
        productLogger.info("Fetching products for store {} - page: {}, limit: {}, query: {}, category: {}", 
                          storeId, page, limit, query, category);
        
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new IllegalArgumentException("Store not found with id: " + storeId));
        
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Product> productPage = productRepository.findProductsByStoreWithFilters(
                storeId, query, category, minPrice, maxPrice, pageable);

        List<ProductPublicDto> productList = productPage.getContent()
                .stream()
                .map(productMapper::toPublicDto)
                .toList();

        PaginatedResponseDto.MetaData meta = new PaginatedResponseDto.MetaData(
                page, 
                limit, 
                productPage.getTotalElements()
        );

        return new PaginatedResponseDto<>(meta, productList);
    }

    public ProductDetailDto getProductById(UUID productId) {
        productLogger.info("Fetching product details for ID: {}", productId);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found with id: " + productId));

        return productMapper.toDetailDto(product);
    }

    public void deleteProduct(String storeEmail, UUID productId) {
        productLogger.info("Deleting product {} for store: {}", productId, storeEmail);
        
        Store store = storeRepository.findByEmail(storeEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Store not found with email: " + storeEmail));

        Product product = productRepository.findByIdAndStoreId(productId, store.getId())
                .orElseThrow(() -> new IllegalArgumentException("Product not found or doesn't belong to store"));

        productRepository.delete(product);
        productLogger.info("Product {} deleted successfully", productId);
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("Image file size exceeds 5MB limit");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only JPG, PNG, and WEBP images are allowed");
        }
    }
}