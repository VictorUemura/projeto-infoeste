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
                .components(
                        new Components()
                                .addSecuritySchemes(securitySchemeName,
                                        new SecurityScheme()
                                                .name(securitySchemeName)
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                                .description("Token JWT obtido através do endpoint de login")
                                                .in(SecurityScheme.In.HEADER)
                                )
                )
                // Aplicar segurança globalmente para todos os endpoints
                .security(List.of(new SecurityRequirement().addList(securitySchemeName)))
                .info(new Info()
                        .title("INFOESTE API")
                        .version(apiVersion)
                        .description("""
                            API REST para o sistema INFOESTE - Plataforma de E-commerce para Lojas
                            
                            1. Registre uma nova loja usando `/v1/stores/register`
                            2. Faça login com `/v1/stores/login` para obter o token JWT
                            3. Use o token no header `Authorization: Bearer <token>` para endpoints protegidos
                            4. Gerencie produtos, visualize lojas e utilize os filtros disponíveis
                            
                            ## Formato de Imagens:
                            - Formatos aceitos: JPG, PNG, WEBP
                            - Tamanho máximo: 5MB
                            - Imagens são armazenadas em Base64
                            """)
                );
    }

}
