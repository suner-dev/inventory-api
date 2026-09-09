-- =============================================================================
-- Ajout de la colonne version pour l'optimistic locking JPA (@Version)
-- =============================================================================

ALTER TABLE products ADD COLUMN version BIGINT NOT NULL DEFAULT 0;
