-- =============================================================================
-- Produits supplementaires pour la demonstration
-- =============================================================================

INSERT INTO products (name, price, stock_quantity, created_at, updated_at) VALUES
('Clavier sans fil',    45000.00,   3,  NOW() - INTERVAL '1 day',   NULL),
('Souris gaming',       35000.00,   7,  NOW() - INTERVAL '1 day',   NULL),
('Moniteur 32 pouces',  450000.00,  2,  NOW() - INTERVAL '1 day',   NULL),
('Casque Bluetooth',    125000.00,  15, NOW() - INTERVAL '1 day',   NOW() - INTERVAL '2 hours'),
('Batterie externe',    28000.00,   4,  NOW() - INTERVAL '1 day',   NULL);
