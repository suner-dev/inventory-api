package com.example.inventoryapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI inventoryOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Inventory API")
                        .version("1.0.0")
                        .description("""
                                API REST de gestion d'un inventaire de produits avec suivi des stocks.

                                - CRUD complet des produits (nom, prix en BigDecimal, quantite en stock).
                                - Alerte stock faible : un produit est en stock faible strictement en dessous de 5 unites (configurable).
                                - Persistance PostgreSQL via Spring Data JPA et migrations Flyway.
                                - Dates generees cote serveur (createdAt, updatedAt).
                                """)
                        .contact(new Contact().name("API Support").email("support@example.com"))
                        .license(new License().name("MIT")));
    }
}
