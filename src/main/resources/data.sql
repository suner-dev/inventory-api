-- =============================================================================
-- Donnees de demonstration pour PostgreSQL (chargement manuel)
-- Usage : psql -U inventory -d inventorydb -f src/main/resources/data.sql
-- =============================================================================

-- Attention : ce fichier est une alternative manuelle au seed Flyway (V2__seed.sql).
-- Si vous utilisez Flyway (defaut), NE PAS charger ce fichier manuellement.

INSERT INTO products (name, price, stock_quantity, created_at, updated_at) VALUES
('Laptop Pro 16',      1250000.00, 10, NOW() - INTERVAL '2 days',  NOW() - INTERVAL '1 day'),
('Smartphone Galaxy',  650000.00,  3,  NOW() - INTERVAL '2 days',  NULL),
('Souris sans fil',    25000.00,   2,  NOW() - INTERVAL '2 days',  NULL),
('Clavier mecanique',  85000.00,   8,  NOW() - INTERVAL '2 days',  NOW() - INTERVAL '12 hours'),
('Ecran 27 pouces',    275000.00,  4,  NOW() - INTERVAL '2 days',  NULL),
('Casque audio',       95000.00,   12, NOW() - INTERVAL '2 days',  NULL),
('Webcam HD',          45000.00,   0,  NOW() - INTERVAL '2 days',  NULL),
('Disque SSD 1 To',    110000.00,  5,  NOW() - INTERVAL '2 days',  NOW() - INTERVAL '6 hours'),
('Kit bureau + chaise', 189000.00,  1,  NOW() - INTERVAL '2 days',  NULL),
('Hub USB-C 7 ports',   32000.00,   20, NOW() - INTERVAL '2 days',  NULL),
('Clavier sans fil',    45000.00,   3,  NOW() - INTERVAL '1 day',   NULL),
('Souris gaming',       35000.00,   7,  NOW() - INTERVAL '1 day',   NULL),
('Moniteur 32 pouces',  450000.00,  2,  NOW() - INTERVAL '1 day',   NULL),
('Casque Bluetooth',    125000.00,  15, NOW() - INTERVAL '1 day',   NOW() - INTERVAL '2 hours'),
('Batterie externe',    28000.00,   4,  NOW() - INTERVAL '1 day',   NULL);
