package com.umdev.infoeste.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    @Value("${spring.application.version}")
    private String apiVersion;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .servers(List.of(
                    new Server().url("http://localhost:8080").description("Servidor de Desenvolvimento"),
                    new Server().url("https://api.infoeste.com").description("Servidor de Produção")
                ))
                .components(
                        new Components()
                                .addSecuritySchemes(securitySchemeName,
                                        new SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                                .description("Token JWT obtido através do endpoint de login")
                                )
                )
                .info(new Info()
                        .title("INFOESTE API")
                        .version(apiVersion)
                        .description("""
                            API REST para o sistema INFOESTE - Plataforma de E-commerce para Lojas
                            
                            ## Funcionalidades Principais:
                            - **Autenticação JWT**: Sistema de login seguro para lojas
                            - **Gerenciamento de Produtos**: CRUD completo com upload de imagens
                            - **Gerenciamento de Lojas**: Registro, perfil e listagem pública
                            - **Busca e Filtros**: Sistema avançado de busca por produtos e lojas
                            - **Paginação**: Todas as listagens são paginadas para melhor performance
                            
                            ## Como Usar:
                            1. Registre uma nova loja usando `/v1/stores/register`
                            2. Faça login com `/v1/stores/login` para obter o token JWT
                            3. Use o token no header `Authorization: Bearer <token>` para endpoints protegidos
                            4. Gerencie produtos, visualize lojas e utilize os filtros disponíveis
                            
                            ## Formato de Imagens:
                            - Formatos aceitos: JPG, PNG, WEBP
                            - Tamanho máximo: 5MB
                            - Imagens são armazenadas em Base64
                            """)
                        .contact(new Contact()
                                .name("Equipe INFOESTE")
                                .email("contato@infoeste.com")
                                .url("https://www.infoeste.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT"))
                );
    }

}
