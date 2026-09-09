package com.example.inventoryapi.service;

import com.example.inventoryapi.dto.ProductCreateRequest;
import com.example.inventoryapi.dto.ProductResponse;
import com.example.inventoryapi.dto.ProductUpdateRequest;

import java.util.List;

/**
 * Contrat du service metier des produits.
 * L'implementation porte toute la regle metier (dont le seuil de stock faible).
 */
public interface ProductService {

    ProductResponse createProduct(ProductCreateRequest request);

    List<ProductResponse> getAllProducts();

    List<ProductResponse> getLowStockProducts();

    ProductResponse getProductById(Long id);

    ProductResponse updateProduct(Long id, ProductUpdateRequest request);

    void deleteProduct(Long id);
}
