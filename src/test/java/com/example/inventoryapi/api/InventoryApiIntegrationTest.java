package com.example.inventoryapi.api;

import com.example.inventoryapi.entity.Product;
import com.example.inventoryapi.repository.ProductRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests d'integration sur PostgreSQL reel (inventorydb_test, profil test).
 * Deroule le scenario complet en 8 etapes du cahier des charges + validation
 * + cas limites du seuil de stock faible (0, 1, 4, 5, 6).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class InventoryApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ProductRepository productRepository;

    private static Long laptopId;

    // ---- ETAPE 1 : creer Laptop -> 201 ----
    @Test
    @Order(1)
    void step1_createLaptop_returns201() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content("{\"name\":\"Laptop\",\"price\":750000.00,\"stockQuantity\":10}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.lowStock").value(false))
                .andExpect(jsonPath("$.createdAt").exists())
                .andReturn();
        laptopId = com.jayway.jsonpath.JsonPath.parse(result.getResponse().getContentAsString())
                .read("$.id", Long.class);
    }

    // ---- ETAPE 2 : la liste contient Laptop ----
    @Test
    @Order(2)
    void step2_listContainsLaptop() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.name == 'Laptop')]").isNotEmpty())
                .andExpect(jsonPath("$[*].id").exists());
    }

    // ---- ETAPE 3 : recuperer Laptop par id -> 200 ----
    @Test
    @Order(3)
    void step3_getLaptopById_returns200() throws Exception {
        mockMvc.perform(get("/api/products/{id}", laptopId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(laptopId))
                .andExpect(jsonPath("$.stockQuantity").value(10));
    }

    // ---- ETAPE 4 : modifier stock = 3 -> 200 ----
    @Test
    @Order(4)
    void step4_updateStockTo3_returns200() throws Exception {
        mockMvc.perform(put("/api/products/{id}", laptopId)
                        .contentType("application/json")
                        .content("{\"name\":\"Laptop\",\"price\":750000.00,\"stockQuantity\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stockQuantity").value(3))
                .andExpect(jsonPath("$.lowStock").value(true))
                .andExpect(jsonPath("$.updatedAt").isNotEmpty());
    }

    // ---- ETAPE 5 : stock 3 -> apparait dans low-stock ----
    @Test
    @Order(5)
    void step5_laptopAppearsInLowStock() throws Exception {
        mockMvc.perform(get("/api/products/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == %d)]", laptopId).isNotEmpty());
    }

    // ---- ETAPE 6 : stock = 5 -> n'apparait PLUS dans low-stock ----
    @Test
    @Order(6)
    void step6_stock5_removedFromLowStock() throws Exception {
        mockMvc.perform(put("/api/products/{id}", laptopId)
                        .contentType("application/json")
                        .content("{\"name\":\"Laptop\",\"price\":750000.00,\"stockQuantity\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lowStock").value(false));

        mockMvc.perform(get("/api/products/low-stock"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == %d)]", laptopId).isEmpty());
    }

    // ---- ETAPE 7 : supprimer Laptop -> 204 ----
    @Test
    @Order(7)
    void step7_deleteLaptop_returns204() throws Exception {
        mockMvc.perform(delete("/api/products/{id}", laptopId))
                .andExpect(status().isNoContent());
    }

    // ---- ETAPE 8 : produit supprime -> 404 ----
    @Test
    @Order(8)
    void step8_getDeletedLaptop_returns404() throws Exception {
        mockMvc.perform(get("/api/products/{id}", laptopId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Product with id " + laptopId + " not found"));
        // plus aucune trace en base
        assertThat(productRepository.findById(laptopId)).isEmpty();
    }

    // ------------------------------------------------------------------
    // Validation des donnees
    // ------------------------------------------------------------------

    @Test
    void blankName_returns400() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content("{\"name\":\"  \",\"price\":10.00,\"stockQuantity\":5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void nullPrice_returns400() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content("{\"name\":\"Test\",\"stockQuantity\":5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.price").exists());
    }

    @Test
    void negativePrice_returns400() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content("{\"name\":\"Test\",\"price\":-10.00,\"stockQuantity\":5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.price").exists());
    }

    @Test
    void zeroPrice_returns400() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content("{\"name\":\"Test\",\"price\":0.00,\"stockQuantity\":5}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.price").exists());
    }

    @Test
    void negativeStock_returns400() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content("{\"name\":\"Test\",\"price\":10.00,\"stockQuantity\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.stockQuantity").exists());
    }

    @Test
    void malformedJson_returns400() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content("{ invalid json"))
                .andExpect(status().isBadRequest());
    }

    // ------------------------------------------------------------------
    // Cas limites du seuil de stock faible (0, 1, 4 -> low ; 5, 6 -> normal)
    // ------------------------------------------------------------------

    @Test
    void lowStockBoundaries_zeroOneFourAreLowFiveSixAreNormal() throws Exception {
        long zero = createProduct("P0", "0");
        long one = createProduct("P1", "1");
        long four = createProduct("P4", "4");
        long five = createProduct("P5", "5");
        long six = createProduct("P6", "6");

        MvcResult result = mockMvc.perform(get("/api/products/low-stock"))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        assertThat(body).contains("\"id\":" + zero, "\"id\":" + one, "\"id\":" + four);
        assertThat(body).doesNotContain("\"id\":" + five, "\"id\":" + six);
    }

    // ---- Filtre optionnel lowStock=true -------
    @Test
    void lowStockFilterParam_returnsOnlyLowStock() throws Exception {
        createProduct("Filtre bas", "2");
        createProduct("Filtre haut", "9");

        String all = mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        String filtered = mockMvc.perform(get("/api/products").param("lowStock", "true"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        assertThat(filtered.length()).isLessThan(all.length());
        assertThat(filtered).doesNotContain("Filtre haut");
    }

    // ---- Persistance reelle : le produit existe en base PostgreSQL ----
    @Test
    void productIsActuallyPersistedInPostgres() throws Exception {
        long id = createProduct("Persiste", "12");
        Integer count = jdbcTemplate.queryForObject(
                "SELECT stock_quantity FROM products WHERE id = ?", Integer.class, id);
        assertThat(count).isEqualTo(12);
    }

    // ---- Swagger expose tous les endpoints ----
    @Test
    void swaggerExposesAllEndpoints() throws Exception {
        String body = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        assertThat(body).contains("/api/products", "/api/products/{id}", "/api/products/low-stock");
    }

    private long createProduct(String name, String stock) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/products")
                        .contentType("application/json")
                        .content("{\"name\":\"" + name + "\",\"price\":100.00,\"stockQuantity\":" + stock + "}"))
                .andExpect(status().isCreated()).andReturn();
        return com.jayway.jsonpath.JsonPath.parse(result.getResponse().getContentAsString())
                .read("$.id", Long.class);
    }
}
