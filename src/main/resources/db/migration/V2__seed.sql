-- =============================================================================
-- Seed de demonstration : produits realistes avec stocks melanges
-- (certains en stock faible < 5, d'autres normaux).
-- Applique UNE SEULE FOIS par Flyway au premier demarrage (base neuve).
-- =============================================================================

INSERT INTO products (name, price, stock_quantity, created_at, updated_at) VALUES
('Laptop Pro 16',      1250000.00, 10, NOW() - INTERVAL '2 days',  NULL),
('Smartphone Galaxy',  650000.00,  3,  NOW() - INTERVAL '2 days',  NULL),
('Souris sans fil',    25000.00,   2,  NOW() - INTERVAL '2 days',  NULL),
('Clavier mecanique',  85000.00,   8,  NOW() - INTERVAL '2 days',  NULL),
('Ecran 27 pouces',    275000.00,  4,  NOW() - INTERVAL '2 days',  NULL),
('Casque audio',       95000.00,   12, NOW() - INTERVAL '2 days',  NULL),
('Webcam HD',          45000.00,   0,  NOW() - INTERVAL '2 days',  NULL),
('Disque SSD 1 To',    110000.00,  5,  NOW() - INTERVAL '2 days',  NULL),
('Kit bureau + chaise', 189000.00,  1,  NOW() - INTERVAL '2 days',  NULL),
('Hub USB-C 7 ports',   32000.00,   20, NOW() - INTERVAL '2 days',  NULL);
