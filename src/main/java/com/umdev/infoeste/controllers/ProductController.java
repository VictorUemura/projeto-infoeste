package com.umdev.infoeste.controllers;

import com.umdev.infoeste.dto.*;
import com.umdev.infoeste.services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/products")
@CrossOrigin(origins = "*")
@Validated
@Tag(name = "Products", description = "API de gerenciamento de produtos")
public class ProductController {

    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(
        summary = "Criar novo produto",
        description = "Cria um novo produto com imagem para a loja autenticada. Requer autenticação JWT.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Produto criado com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ProductCreateResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados inválidos ou arquivo de imagem inválido",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = {
                    @ExampleObject(name = "Validation Error", value = """
                        {
                          "timestamp": "2025-10-29T10:30:00-03:00",
                          "path": "uri=/v1/products",
                          "status": 400,
                          "error": "Bad Request",
                          "message": "Validation failed for one or more fields.",
                          "validationErrors": [
                            {"field": "name", "message": "Name is required"},
                            {"field": "price", "message": "Price must be greater than 0"}
                          ]
                        }
                        """),
                    @ExampleObject(name = "File Error", value = """
                        {
                          "timestamp": "2025-10-29T10:30:00-03:00",
                          "path": "uri=/v1/products",
                          "status": 400,
                          "error": "Bad Request",
                          "message": "Only JPG, PNG, and WEBP images are allowed"
                        }
                        """)
                }
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token JWT inválido, ausente ou expirado",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = """
                    {
                      "timestamp": "2025-10-29T10:30:00-03:00",
                      "path": "uri=/v1/products",
                      "status": 401,
                      "error": "Unauthorized",
                      "message": "Token expired. Please login again."
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Loja não encontrada com o email do token",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = """
                    {
                      "timestamp": "2025-10-29T10:30:00-03:00",
                      "path": "uri=/v1/products",
                      "status": 404,
                      "error": "Not Found",
                      "message": "Store not found with email: store@example.com"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "413",
            description = "Arquivo de imagem muito grande (máximo 5MB)",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = """
                    {
                      "timestamp": "2025-10-29T10:30:00-03:00",
                      "path": "uri=/v1/products",
                      "status": 413,
                      "error": "Payload Too Large",
                      "message": "File size exceeds maximum allowed limit"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro interno no processamento da imagem",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = """
                    {
                      "timestamp": "2025-10-29T10:30:00-03:00",
                      "path": "uri=/v1/products",
                      "status": 500,
                      "error": "Internal Server Error",
                      "message": "Internal processing error: Error processing image file"
                    }
                    """)
            )
        )
    })
    public ResponseEntity<ProductCreateResponseDto> createProduct(
            Authentication authentication,
            @Parameter(description = "Nome do produto", required = true, example = "Notebook Gamer")
            @RequestPart("name") @NotBlank(message = "Name is required") String name,
            
            @Parameter(description = "Descrição detalhada do produto", example = "Notebook gamer de alta performance com placa de vídeo dedicada")
            @RequestPart(value = "description", required = false) String description,
            
            @Parameter(description = "Preço do produto em reais", required = true, example = "2999.99")
            @RequestPart("price") @NotNull(message = "Price is required")
            @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0") BigDecimal price,
            
            @Parameter(description = "Quantidade em estoque", required = true, example = "50")
            @RequestPart("stock") @NotNull(message = "Stock is required")
            @Min(value = 0, message = "Stock must be 0 or greater") Integer stock,
            
            @Parameter(description = "Categoria do produto", example = "Eletrônicos")
            @RequestPart(value = "category", required = false) String category,
            
            @Parameter(description = "Imagem do produto (JPG, PNG ou WEBP, máximo 5MB)", required = true)
            @RequestPart("file") MultipartFile file) {
        
        logger.info("Received createProduct request - name: {}, price: {}, file: {}", name, price, file.getOriginalFilename());
        logger.info("File details - size: {}, contentType: {}", file.getSize(), file.getContentType());
        
        ProductCreateDto productDto = new ProductCreateDto(name, description, price, stock, category);
        
        String storeEmail = authentication.getName();
        ProductCreateResponseDto response = productService.createProduct(storeEmail, productDto, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    @Operation(
        summary = "Listar meus produtos",
        description = "Retorna a lista de produtos da loja autenticada. Requer autenticação JWT.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de produtos retornada com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(
                    type = "array",
                    implementation = ProductMyListDto.class
                ),
                examples = @ExampleObject(value = """
                    [
                      {
                        "id": "123e4567-e89b-12d3-a456-426614174000",
                        "name": "Notebook Gamer",
                        "price": 2999.99,
                        "stock": 15,
                        "category": "Eletrônicos",
                        "createdAt": "2025-10-29T10:30:00"
                      },
                      {
                        "id": "987fcdeb-51a2-43e1-b789-123456789abc",
                        "name": "Mouse Gaming",
                        "price": 149.90,
                        "stock": 50,
                        "category": "Periféricos",
                        "createdAt": "2025-10-28T15:20:00"
                      }
                    ]
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token JWT inválido, ausente ou expirado",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = """
                    {
                      "timestamp": "2025-10-29T10:30:00-03:00",
                      "path": "uri=/v1/products/my",
                      "status": 401,
                      "error": "Unauthorized",
                      "message": "Token expired. Please login again."
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Loja não encontrada com o email do token",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = """
                    {
                      "timestamp": "2025-10-29T10:30:00-03:00",
                      "path": "uri=/v1/products/my",
                      "status": 404,
                      "error": "Not Found",
                      "message": "Store not found with email: store@example.com"
                    }
                    """)
            )
        )
    })
    public ResponseEntity<List<ProductMyListDto>> getMyProducts(Authentication authentication) {
        String storeEmail = authentication.getName();
        List<ProductMyListDto> products = productService.getMyProducts(storeEmail);
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{productId}")
    @Operation(
        summary = "Atualizar produto",
        description = "Atualiza as informações de um produto específico da loja autenticada. Requer autenticação JWT.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Produto atualizado com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ProductCreateResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados de atualização inválidos",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = """
                    {
                      "timestamp": "2025-10-29T10:30:00-03:00",
                      "path": "uri=/v1/products/123e4567-e89b-12d3-a456-426614174000",
                      "status": 400,
                      "error": "Bad Request",
                      "message": "Validation failed for one or more fields.",
                      "validationErrors": [
                        {"field": "price", "message": "Price must be greater than 0"}
                      ]
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token JWT inválido, ausente ou expirado"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Produto não encontrado ou não pertence à loja autenticada",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = {
                    @ExampleObject(name = "Product Not Found", value = """
                        {
                          "timestamp": "2025-10-29T10:30:00-03:00",
                          "path": "uri=/v1/products/123e4567-e89b-12d3-a456-426614174000",
                          "status": 404,
                          "error": "Not Found",
                          "message": "Product not found or doesn't belong to store"
                        }
                        """),
                    @ExampleObject(name = "Store Not Found", value = """
                        {
                          "timestamp": "2025-10-29T10:30:00-03:00",
                          "path": "uri=/v1/products/123e4567-e89b-12d3-a456-426614174000",
                          "status": 404,
                          "error": "Not Found",
                          "message": "Store not found with email: store@example.com"
                        }
                        """)
                }
            )
        )
    })
    public ResponseEntity<ProductCreateResponseDto> updateProduct(
            Authentication authentication,
            @Parameter(description = "ID único do produto", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID productId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Dados para atualização do produto",
                required = true,
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = ProductUpdateDto.class),
                    examples = @ExampleObject(value = """
                        {
                          "name": "Notebook Gamer Atualizado",
                          "price": 3199.99,
                          "stock": 25,
                          "category": "Eletrônicos"
                        }
                        """)
                )
            )
            @RequestBody ProductUpdateDto updateDto) {
        
        String storeEmail = authentication.getName();
        ProductCreateResponseDto response = productService.updateProduct(storeEmail, productId, updateDto);
        return ResponseEntity.ok(response);
    }

    @PutMapping(value = "/{productId}/image", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(
        summary = "Atualizar imagem do produto",
        description = "Atualiza a imagem de um produto específico da loja autenticada. Requer autenticação JWT.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Imagem do produto atualizada com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ProductImageUpdateResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Arquivo de imagem inválido"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token JWT inválido ou ausente"
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Produto não encontrado ou não pertence à loja"
        ),
        @ApiResponse(
            responseCode = "413",
            description = "Arquivo de imagem muito grande (máximo 5MB)"
        )
    })
    public ResponseEntity<ProductImageUpdateResponseDto> updateProductImage(
            Authentication authentication,
            @Parameter(description = "ID único do produto", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID productId,
            @Parameter(description = "Nova imagem do produto (JPG, PNG ou WEBP, máximo 5MB)", required = true)
            @RequestPart("file") MultipartFile file) {
        
        String storeEmail = authentication.getName();
        ProductImageUpdateResponseDto response = productService.updateProductImage(storeEmail, productId, file);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(
        summary = "Listar produtos públicos",
        description = "Retorna uma lista paginada de produtos públicos com filtros opcionais. Não requer autenticação."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de produtos retornada com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = PaginatedResponseDto.class),
                examples = @ExampleObject(value = """
                    {
                      "meta": {
                        "page": 1,
                        "limit": 10,
                        "total": 25
                      },
                      "data": [
                        {
                          "id": "123e4567-e89b-12d3-a456-426614174000",
                          "name": "Notebook Gamer",
                          "price": 2999.99,
                          "category": "Eletrônicos",
                          "imageBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRgABA...",
                          "store": {
                            "id": "store-uuid",
                            "name": "Tech Store",
                            "city": "São Paulo"
                          }
                        },
                        {
                          "id": "987fcdeb-51a2-43e1-b789-123456789abc",
                          "name": "Mouse Gaming",
                          "price": 149.90,
                          "category": "Periféricos",
                          "imageBase64": "data:image/png;base64,iVBORw0KGgoAAA...",
                          "store": {
                            "id": "store-uuid-2",
                            "name": "Gamer Store",
                            "city": "Rio de Janeiro"
                          }
                        }
                      ]
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Parâmetros de consulta inválidos"
        )
    })
    public ResponseEntity<PaginatedResponseDto<ProductPublicDto>> getProducts(
            @Parameter(description = "Número da página (começando em 1)", example = "1")
            @RequestParam(defaultValue = "1") int page,
            
            @Parameter(description = "Número de itens por página", example = "10")
            @RequestParam(defaultValue = "10") int limit,
            
            @Parameter(description = "Termo de busca no nome do produto", example = "notebook")
            @RequestParam(required = false) String q,
            
            @Parameter(description = "Filtrar por categoria", example = "Eletrônicos")
            @RequestParam(required = false) String category,
            
            @Parameter(description = "Preço mínimo para filtro", example = "100.00")
            @RequestParam(required = false) BigDecimal minPrice,
            
            @Parameter(description = "Preço máximo para filtro", example = "5000.00")
            @RequestParam(required = false) BigDecimal maxPrice) {
        
        PaginatedResponseDto<ProductPublicDto> response = productService.getProducts(
                page, limit, q, category, minPrice, maxPrice);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/store/{storeId}")
    @Operation(
        summary = "Listar produtos de uma loja específica",
        description = "Retorna uma lista paginada de produtos de uma loja específica com filtros opcionais. Não requer autenticação."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de produtos da loja retornada com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = PaginatedResponseDto.class),
                examples = @ExampleObject(value = """
                    {
                      "meta": {
                        "page": 1,
                        "limit": 10,
                        "total": 8
                      },
                      "data": [
                        {
                          "id": "123e4567-e89b-12d3-a456-426614174000",
                          "name": "Notebook Gamer",
                          "price": 2999.99,
                          "category": "Eletrônicos",
                          "imageBase64": "data:image/jpeg;base64,/9j/4AAQSkZJRgABA...",
                          "store": {
                            "id": "store-uuid",
                            "name": "Tech Store",
                            "city": "São Paulo"
                          }
                        }
                      ]
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Parâmetros de consulta inválidos",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = """
                    {
                      "timestamp": "2025-10-29T10:30:00-03:00",
                      "path": "uri=/v1/products/store/invalid-uuid",
                      "status": 400,
                      "error": "Bad Request",
                      "message": "Invalid UUID format"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Loja não encontrada",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = """
                    {
                      "timestamp": "2025-10-29T10:30:00-03:00",
                      "path": "uri=/v1/products/store/123e4567-e89b-12d3-a456-426614174000",
                      "status": 404,
                      "error": "Not Found",
                      "message": "Store not found with id: 123e4567-e89b-12d3-a456-426614174000"
                    }
                    """)
            )
        )
    })
    public ResponseEntity<PaginatedResponseDto<ProductPublicDto>> getProductsByStore(
            @Parameter(description = "ID único da loja", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID storeId,
            
            @Parameter(description = "Número da página (começando em 1)", example = "1")
            @RequestParam(defaultValue = "1") int page,
            
            @Parameter(description = "Número de itens por página", example = "10")
            @RequestParam(defaultValue = "10") int limit,
            
            @Parameter(description = "Termo de busca no nome do produto", example = "notebook")
            @RequestParam(required = false) String q,
            
            @Parameter(description = "Filtrar por categoria", example = "Eletrônicos")
            @RequestParam(required = false) String category,
            
            @Parameter(description = "Preço mínimo para filtro", example = "100.00")
            @RequestParam(required = false) BigDecimal minPrice,
            
            @Parameter(description = "Preço máximo para filtro", example = "5000.00")
            @RequestParam(required = false) BigDecimal maxPrice) {
        
        PaginatedResponseDto<ProductPublicDto> response = productService.getProductsByStore(
                storeId, page, limit, q, category, minPrice, maxPrice);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{productId}")
    @Operation(
        summary = "Obter detalhes de um produto",
        description = "Retorna os detalhes completos de um produto específico. Não requer autenticação."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Detalhes do produto retornados com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = ProductDetailDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "ID do produto em formato inválido",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = """
                    {
                      "timestamp": "2025-10-29T10:30:00-03:00",
                      "path": "uri=/v1/products/invalid-uuid",
                      "status": 400,
                      "error": "Bad Request",
                      "message": "Invalid UUID format"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Produto não encontrado",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = """
                    {
                      "timestamp": "2025-10-29T10:30:00-03:00",
                      "path": "uri=/v1/products/123e4567-e89b-12d3-a456-426614174000",
                      "status": 404,
                      "error": "Not Found",
                      "message": "Product not found with id: 123e4567-e89b-12d3-a456-426614174000"
                    }
                    """)
            )
        )
    })
    public ResponseEntity<ProductDetailDto> getProductById(
            @Parameter(description = "ID único do produto", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID productId) {
        ProductDetailDto product = productService.getProductById(productId);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{productId}")
    @Operation(
        summary = "Excluir produto",
        description = "Exclui um produto específico da loja autenticada. Requer autenticação JWT.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Produto excluído com sucesso"
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token JWT inválido, ausente ou expirado",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = """
                    {
                      "timestamp": "2025-10-29T10:30:00-03:00",
                      "path": "uri=/v1/products/123e4567-e89b-12d3-a456-426614174000",
                      "status": 401,
                      "error": "Unauthorized",
                      "message": "Token expired. Please login again."
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Produto não encontrado ou não pertence à loja autenticada",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = {
                    @ExampleObject(name = "Product Not Found", value = """
                        {
                          "timestamp": "2025-10-29T10:30:00-03:00",
                          "path": "uri=/v1/products/123e4567-e89b-12d3-a456-426614174000",
                          "status": 404,
                          "error": "Not Found",
                          "message": "Product not found or doesn't belong to store"
                        }
                        """),
                    @ExampleObject(name = "Store Not Found", value = """
                        {
                          "timestamp": "2025-10-29T10:30:00-03:00",
                          "path": "uri=/v1/products/123e4567-e89b-12d3-a456-426614174000",
                          "status": 404,
                          "error": "Not Found",
                          "message": "Store not found with email: store@example.com"
                        }
                        """)
                }
            )
        )
    })
    public ResponseEntity<Void> deleteProduct(
            Authentication authentication,
            @Parameter(description = "ID único do produto", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID productId) {
        
        String storeEmail = authentication.getName();
        productService.deleteProduct(storeEmail, productId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping(value = "/test-multipart", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(
        summary = "Test multipart endpoint",
        description = "Simple endpoint to test multipart form data handling",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<String> testMultipart(
            Authentication authentication,
            @RequestPart("name") String name,
            @RequestPart("file") MultipartFile file) {
        
        logger.info("Test multipart - name: {}, file: {}, size: {}, contentType: {}", 
                name, file.getOriginalFilename(), file.getSize(), file.getContentType());
        
        return ResponseEntity.ok("Success - name: " + name + ", file: " + file.getOriginalFilename());
    }

    @PostMapping(value = "/debug-simple", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @Operation(
        summary = "Debug simple multipart",
        description = "Debug multipart without authentication"
    )
    public ResponseEntity<String> debugSimple(
            @RequestPart("name") String name,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        
        logger.info("Debug simple - name: {}", name);
        if (file != null) {
            logger.info("File details - name: {}, size: {}, contentType: {}", 
                    file.getOriginalFilename(), file.getSize(), file.getContentType());
            return ResponseEntity.ok("Success - name: " + name + ", file: " + file.getOriginalFilename());
        } else {
            return ResponseEntity.ok("Success - name: " + name + ", no file");
        }
    }
}