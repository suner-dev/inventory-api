package com.example.inventoryapi.service;

import com.example.inventoryapi.dto.ProductCreateRequest;
import com.example.inventoryapi.dto.ProductResponse;
import com.example.inventoryapi.dto.ProductUpdateRequest;
import com.example.inventoryapi.entity.Product;
import com.example.inventoryapi.exception.ResourceNotFoundException;
import com.example.inventoryapi.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

    /**
     * Seuil de stock faible, centralise et configurable
     * (propriete inventory.low-stock-threshold). Aucune duplication ailleurs. :
     */
    @Value("${inventory.low-stock-threshold:5}")
    private int lowStockThreshold;

    private final ProductRepository productRepository;

    @Transactional
    @Override
    public ProductResponse createProduct(ProductCreateRequest request) {
        Product product = Product.builder()
                .name(request.name().trim())
                .price(request.price())
                .stockQuantity(request.stockQuantity())
                .build();
        Product saved = productRepository.save(product);
        log.info("Produit cree : id={}, name={}, stock={}", saved.getId(), saved.getName(), saved.getStockQuantity());
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Produits en stock faible : le filtrage est effectue par la base de donnees
     * (findByStockQuantityLessThan), jamais en Java sur la totalite du stock.
     */
    @Transactional(readOnly = true)
    @Override
    public List<ProductResponse> getLowStockProducts() {
        return productRepository.findByStockQuantityLessThan(lowStockThreshold).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    @Override
    public ProductResponse getProductById(Long id) {
        return toResponse(findProduct(id));
    }

    @Transactional
    @Override
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        Product product = findProduct(id);
        product.setName(request.name().trim());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());
        // updatedAt est renseigne par @PreUpdate ; createdAt et id sont preserves
        Product updated = productRepository.saveAndFlush(product);
        log.info("Produit modifie : id={}, stock={}", updated.getId(), updated.getStockQuantity());
        return toResponse(updated);
    }

    @Transactional
    @Override
    public void deleteProduct(Long id) {
        Product product = findProduct(id);
        productRepository.delete(product);
        log.info("Produit supprime : id={}", id);
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(Product.class, id));
    }

    /**
     * La regle de stock faible est centralisee ici : le client sait immediatement,
     * via {@code lowStock}, si le produit doit etre reapprovisionne.
     */
    private ProductResponse toResponse(Product product) {
        return ProductResponse.from(product, lowStockThreshold);
    }
}
