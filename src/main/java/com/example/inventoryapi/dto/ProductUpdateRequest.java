package com.example.inventoryapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Requete de mise a jour d'un produit (nom, prix, quantite)")
public record ProductUpdateRequest(

        @NotBlank(message = "Name is required")
        @Size(max = 150, message = "Name must be at most 150 characters")
        @Schema(description = "Nom du produit", example = "Laptop Pro")
        String name,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        @Schema(description = "Prix (strictement positif, BigDecimal)", example = "900000.00")
        BigDecimal price,

        @NotNull(message = "Stock quantity is required")
        @Min(value = 0, message = "Stock quantity cannot be negative")
        @Schema(description = "Quantite en stock (>= 0)", example = "7")
        Integer stockQuantity) {
}
