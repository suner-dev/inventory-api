package com.example.inventoryapi.dto;

import com.example.inventoryapi.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Reponse produit — jamais l'entite JPA directement.
 * Le champ {@code lowStock} indique immediatement si le produit
 * nécessite un reapprovisionnement (stock < seuil).
 */
@Schema(description = "Produit de l'inventaire")
public record ProductResponse(

        @Schema(description = "Identifiant", example = "1")
        Long id,

        @Schema(description = "Nom", example = "Laptop")
        String name,

        @Schema(description = "Prix", example = "750000.00")
        BigDecimal price,

        @Schema(description = "Quantite en stock", example = "10")
        Integer stockQuantity,

        @Schema(description = "Stock faible : true si stockQuantity < seuil (5)", example = "false")
        boolean lowStock,

        @Schema(description = "Date de creation", example = "2026-09-09T15:30:00")
        LocalDateTime createdAt,

        @Schema(description = "Date de derniere modification", example = "2026-09-09T16:00:00")
        LocalDateTime updatedAt) {

    public static ProductResponse from(Product product, int lowStockThreshold) {
        return new ProductResponse(product.getId(), product.getName(), product.getPrice(),
                product.getStockQuantity(),
                product.getStockQuantity() != null && product.getStockQuantity() < lowStockThreshold,
                product.getCreatedAt(), product.getUpdatedAt());
    }
}
