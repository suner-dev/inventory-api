package com.example.inventoryapi.repository;

import com.example.inventoryapi.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Produits dont le stock est strictement inferieur au seuil donne.
     * Le filtrage est effectue PAR LA BASE DE DONNEES (aucune sur-requete).
     */
    List<Product> findByStockQuantityLessThan(int threshold);
}
