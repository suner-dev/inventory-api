package com.example.inventoryapi.controller;

import com.example.inventoryapi.dto.ProductCreateRequest;
import com.example.inventoryapi.dto.ProductResponse;
import com.example.inventoryapi.dto.ProductUpdateRequest;
import com.example.inventoryapi.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Produits", description = "Gestion de l'inventaire de produits avec suivi des stocks")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(summary = "Creer un produit",
            description = "Cree un produit (nom, prix strictement positif, stock >= 0). " +
                          "Le prix est un BigDecimal (jamais de double pour un montant).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Produit cree",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponse.class),
                            examples = @ExampleObject(name = "Laptop",
                                    value = "{\"id\":1,\"name\":\"Laptop\",\"price\":750000.00,\"stockQuantity\":10,\"lowStock\":false,\"createdAt\":\"2026-09-09T15:30:00\",\"updatedAt\":null}"))),
            @ApiResponse(responseCode = "400", description = "Donnees invalides",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"status\":400,\"error\":\"VALIDATION_ERROR\",\"message\":\"Validation failed\",\"errors\":{\"name\":\"Name is required\",\"price\":\"Price must be greater than 0\"}}")))
    })
    @PostMapping
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }

    @Operation(summary = "Lister tous les produits",
            description = "Retourne tous les produits avec leur indicateur lowStock. " +
                          "Filtre optionnel : ?lowStock=true ne retourne que les produits en stock faible.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liste des produits (peut etre vide)",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "Exemple",
                                    value = "[{\"id\":1,\"name\":\"Laptop\",\"price\":750000.00,\"stockQuantity\":10,\"lowStock\":false},{\"id\":2,\"name\":\"Mouse\",\"price\":10000.00,\"stockQuantity\":2,\"lowStock\":true}]")))
    })
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts(
            @RequestParam(name = "lowStock", required = false, defaultValue = "false")
            @Schema(description = "true pour ne garder que les produits en stock faible (< 5)", example = "true")
            boolean lowStockFilter) {
        if (lowStockFilter) {
            return ResponseEntity.ok(productService.getLowStockProducts());
        }
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @Operation(summary = "Lister les produits en stock faible",
            description = "Fonctionnalite officielle : retourne uniquement les produits avec stockQuantity < 5. " +
                          "Le filtrage est effectue par la base de donnees.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produits en stock faible",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "Exemple",
                                    value = "[{\"id\":2,\"name\":\"Smartphone Galaxy\",\"price\":650000.00,\"stockQuantity\":3,\"lowStock\":true},{\"id\":3,\"name\":\"Souris sans fil\",\"price\":25000.00,\"stockQuantity\":2,\"lowStock\":true}]")))
    })
    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductResponse>> getLowStockProducts() {
        return ResponseEntity.ok(productService.getLowStockProducts());
    }

    @Operation(summary = "Recuperer un produit par son id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produit trouve"),
            @ApiResponse(responseCode = "404", description = "Produit inexistant",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"status\":404,\"error\":\"NOT_FOUND\",\"message\":\"Product with id 15 not found\"}")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @Operation(summary = "Modifier un produit",
            description = "Modifie nom, prix et quantite. Le stock ne peut jamais etre negatif.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Produit modifie"),
            @ApiResponse(responseCode = "400", description = "Donnees invalides"),
            @ApiResponse(responseCode = "404", description = "Produit inexistant")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable Long id,
                                                         @Valid @RequestBody ProductUpdateRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @Operation(summary = "Supprimer un produit")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Produit supprime"),
            @ApiResponse(responseCode = "404", description = "Produit inexistant")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
