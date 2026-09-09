package com.example.inventoryapi.service;

import com.example.inventoryapi.dto.ProductCreateRequest;
import com.example.inventoryapi.dto.ProductResponse;
import com.example.inventoryapi.dto.ProductUpdateRequest;
import com.example.inventoryapi.entity.Product;
import com.example.inventoryapi.exception.ResourceNotFoundException;
import com.example.inventoryapi.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    private ProductServiceImpl productService;

    private Product laptop;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(productRepository);
        // seuil de stock faible : 5 (valeur reelle utilisée en production)
        ReflectionTestUtils.setField(productService, "lowStockThreshold", 5);

        laptop = Product.builder()
                .id(1L)
                .name("Laptop")
                .price(new BigDecimal("750000.00"))
                .stockQuantity(10)
                .createdAt(LocalDateTime.of(2026, 9, 9, 15, 30))
                .build();
    }

    @Test
    void shouldCreateProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(laptop);

        ProductResponse response = productService.createProduct(
                new ProductCreateRequest("Laptop", new BigDecimal("750000.00"), 10));

        assertEquals(1L, response.id());
        assertEquals("Laptop", response.name());
        assertEquals(0, new BigDecimal("750000.00").compareTo(response.price()));
        assertFalse(response.lowStock());
    }

    @Test
    void shouldGetAllProducts() {
        when(productRepository.findAll()).thenReturn(List.of(laptop));

        List<ProductResponse> products = productService.getAllProducts();

        assertEquals(1, products.size());
        assertEquals("Laptop", products.get(0).name());
    }

    @Test
    void shouldGetProductById() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(laptop));

        ProductResponse response = productService.getProductById(1L);

        assertEquals(1L, response.id());
    }

    @Test
    void shouldThrowNotFoundForUnknownProduct() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.getProductById(99L));
    }

    @Test
    void shouldUpdateProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(laptop));
        when(productRepository.saveAndFlush(any(Product.class))).thenReturn(laptop);

        ProductResponse response = productService.updateProduct(1L,
                new ProductUpdateRequest("Laptop Pro", new BigDecimal("900000.00"), 7));

        assertEquals("Laptop Pro", response.name());
        assertEquals(7, response.stockQuantity());
    }

    @Test
    void shouldThrowNotFoundWhenUpdatingUnknownProduct() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.updateProduct(99L, new ProductUpdateRequest("X", new BigDecimal("1.00"), 1)));
        verify(productRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldDeleteProductById() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(laptop));

        productService.deleteProduct(1L);

        verify(productRepository).delete(laptop);
    }

    @Test
    void shouldThrowNotFoundWhenDeletingUnknownProduct() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(99L));
        verify(productRepository, never()).delete(any());
    }

    // ---- Stock faible : le filtrage est DELEGUE a la base (findByStockQuantityLessThan) ----
    @Test
    void shouldGetLowStockProductsUsingRepositoryFilter() {
        Product mouse = Product.builder().id(3L).name("Mouse").price(new BigDecimal("10000.00"))
                .stockQuantity(2).createdAt(LocalDateTime.now()).build();
        when(productRepository.findByStockQuantityLessThan(anyInt())).thenReturn(List.of(mouse));

        List<ProductResponse> low = productService.getLowStockProducts();

        assertEquals(1, low.size());
        assertEquals("Mouse", low.get(0).name());
        assertTrue(low.get(0).lowStock());
        // verifie que le seuil envoye a la base est bien 5
        verify(productRepository).findByStockQuantityLessThan(5);
    }

    // ---- Regle lowStock dans la reponse : < 5 low, >= 5 normal ----
    @Test
    void shouldMarkLowStockForQuantityBelowFive() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(laptop));
        laptop.setStockQuantity(4);
        assertTrue(productService.getProductById(1L).lowStock());
    }

    @Test
    void shouldNotMarkLowStockForQuantityFive() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(laptop));
        laptop.setStockQuantity(5);
        assertFalse(productService.getProductById(1L).lowStock());
    }
}
