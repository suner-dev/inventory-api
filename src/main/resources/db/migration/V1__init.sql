-- =============================================================================
-- Schema initial : table products (inventaire)
-- =============================================================================

CREATE TABLE products (
    id             BIGSERIAL PRIMARY KEY,
    name           VARCHAR(150) NOT NULL,
    price          NUMERIC(15, 2) NOT NULL,
    stock_quantity INTEGER      NOT NULL CHECK (stock_quantity >= 0),
    created_at     TIMESTAMP    NOT NULL,
    updated_at     TIMESTAMP
);

-- Index utile pour la recherche des stocks faibles par la base
CREATE INDEX idx_products_stock_quantity ON products (stock_quantity);
