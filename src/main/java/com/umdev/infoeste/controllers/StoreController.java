package com.umdev.infoeste.controllers;

import com.umdev.infoeste.dto.*;
import com.umdev.infoeste.services.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/stores")
@CrossOrigin(origins = "*")
@Tag(name = "Stores", description = "API de gerenciamento de lojas")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @PostMapping("/register")
    @Operation(
        summary = "Registrar nova loja",
        description = "Registra uma nova loja no sistema. Não requer autenticação."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Loja registrada com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = StoreRegisterResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados de registro inválidos",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = """
                    {
                      "timestamp": "2025-10-29T10:30:00-03:00",
                      "path": "uri=/v1/stores/register",
                      "status": 400,
                      "error": "Bad Request",
                      "message": "Validation failed for one or more fields.",
                      "validationErrors": [
                        {"field": "email", "message": "Email should be valid"},
                        {"field": "name", "message": "Name is required"},
                        {"field": "password", "message": "Password must be at least 6 characters"}
                      ]
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Email já está em uso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = """
                    {
                      "timestamp": "2025-10-29T10:30:00-03:00",
                      "path": "uri=/v1/stores/register",
                      "status": 409,
                      "error": "Conflict",
                      "message": "Store already exists with email: store@example.com"
                    }
                    """)
            )
        )
    })
    public ResponseEntity<StoreRegisterResponseDto> register(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Dados para registro da loja",
                required = true,
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = StoreRegisterDto.class),
                    examples = @ExampleObject(value = """
                        {
                          "name": "Tech Store",
                          "email": "contato@techstore.com",
                          "password": "senha123",
                          "description": "Loja especializada em produtos tecnológicos",
                          "address": "Rua das Flores, 123",
                          "city": "São Paulo",
                          "phone": "(11) 99999-9999"
                        }
                        """)
                )
            )
            @Valid @RequestBody StoreRegisterDto registerDto) {
        StoreRegisterResponseDto response = storeService.register(registerDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(
        summary = "Login da loja",
        description = "Autentica uma loja no sistema e retorna um token JWT. Não requer autenticação."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Login realizado com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = StoreLoginResponseDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Dados de login inválidos",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = """
                    {
                      "timestamp": "2025-10-29T10:30:00-03:00",
                      "path": "uri=/v1/stores/login",
                      "status": 400,
                      "error": "Bad Request",
                      "message": "Validation failed for one or more fields.",
                      "validationErrors": [
                        {"field": "email", "message": "Email should be valid"},
                        {"field": "password", "message": "Password is required"}
                      ]
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Credenciais inválidas (email ou senha incorretos)",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = """
                    {
                      "timestamp": "2025-10-29T10:30:00-03:00",
                      "path": "uri=/v1/stores/login",
                      "status": 401,
                      "error": "Unauthorized",
                      "message": "Invalid email or password"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Loja não encontrada com o email fornecido",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = """
                    {
                      "timestamp": "2025-10-29T10:30:00-03:00",
                      "path": "uri=/v1/stores/login",
                      "status": 404,
                      "error": "Not Found",
                      "message": "Store not found with email: nonexistent@example.com"
                    }
                    """)
            )
        )
    })
    public ResponseEntity<StoreLoginResponseDto> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Credenciais para login",
                required = true,
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = StoreLoginDto.class),
                    examples = @ExampleObject(value = """
                        {
                          "email": "contato@techstore.com",
                          "password": "senha123"
                        }
                        """)
                )
            )
            @Valid @RequestBody StoreLoginDto loginDto) {
        StoreLoginResponseDto response = storeService.login(loginDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    @Operation(
        summary = "Obter perfil da loja",
        description = "Retorna o perfil da loja autenticada. Requer autenticação JWT.",
        security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Perfil da loja retornado com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = StoreProfileDto.class)
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
                      "path": "uri=/v1/stores/me",
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
                      "path": "uri=/v1/stores/me",
                      "status": 404,
                      "error": "Not Found",
                      "message": "Store not found with email: store@example.com"
                    }
                    """)
            )
        )
    })
    public ResponseEntity<StoreProfileDto> getProfile(Authentication authentication) {
        String email = authentication.getName();
        StoreProfileDto profile = storeService.getProfile(email);
        return ResponseEntity.ok(profile);
    }

    @GetMapping
    @Operation(
        summary = "Listar lojas públicas",
        description = "Retorna uma lista paginada de lojas públicas com filtro de busca opcional. Não requer autenticação."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de lojas retornada com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = PaginatedResponseDto.class),
                examples = @ExampleObject(value = """
                    {
                      "meta": {
                        "page": 1,
                        "limit": 10,
                        "total": 15
                      },
                      "data": [
                        {
                          "id": "store-uuid-1",
                          "name": "Tech Store",
                          "description": "Loja especializada em produtos tecnológicos",
                          "city": "São Paulo",
                          "phone": "(11) 99999-9999"
                        },
                        {
                          "id": "store-uuid-2",
                          "name": "Gamer Store",
                          "description": "Equipamentos para gamers",
                          "city": "Rio de Janeiro",
                          "phone": "(21) 88888-8888"
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
                      "path": "uri=/v1/stores?page=-1",
                      "status": 400,
                      "error": "Bad Request",
                      "message": "Page must be greater than 0"
                    }
                    """)
            )
        )
    })
    public ResponseEntity<PaginatedResponseDto<StorePublicDto>> getStores(
            @Parameter(description = "Número da página (começando em 1)", example = "1")
            @RequestParam(defaultValue = "1") int page,
            
            @Parameter(description = "Número de itens por página", example = "10")
            @RequestParam(defaultValue = "10") int limit,
            
            @Parameter(description = "Termo de busca no nome ou cidade da loja", example = "tech")
            @RequestParam(required = false) String q) {
        
        PaginatedResponseDto<StorePublicDto> response = storeService.getStores(page, limit, q);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{storeId}")
    @Operation(
        summary = "Obter detalhes de uma loja",
        description = "Retorna os detalhes completos de uma loja específica. Não requer autenticação."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Detalhes da loja retornados com sucesso",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = StoreDetailDto.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "ID da loja em formato inválido",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(value = """
                    {
                      "timestamp": "2025-10-29T10:30:00-03:00",
                      "path": "uri=/v1/stores/invalid-uuid",
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
                      "path": "uri=/v1/stores/123e4567-e89b-12d3-a456-426614174000",
                      "status": 404,
                      "error": "Not Found",
                      "message": "Store not found with id: 123e4567-e89b-12d3-a456-426614174000"
                    }
                    """)
            )
        )
    })
    public ResponseEntity<StoreDetailDto> getStoreById(
            @Parameter(description = "ID único da loja", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID storeId) {
        StoreDetailDto storeDetail = storeService.getStoreById(storeId);
        return ResponseEntity.ok(storeDetail);
    }
}