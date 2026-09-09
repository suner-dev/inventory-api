#!/bin/bash
# ============================================================
# Reinitialisation de la base de donnees
# Usage : ./reset-db.sh
# ============================================================

echo "Reinitialisation de la base de donnees..."

PGPASSWORD=postgres psql -h localhost -U postgres -c "DROP DATABASE IF EXISTS inventorydb;" 2>/dev/null
PGPASSWORD=postgres psql -h localhost -U postgres -c "CREATE DATABASE inventorydb;" 2>/dev/null
PGPASSWORD=postgres psql -h localhost -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE inventorydb TO inventory;" 2>/dev/null
PGPASSWORD=postgres psql -h localhost -U postgres -d inventorydb -c "GRANT ALL ON SCHEMA public TO inventory;" 2>/dev/null

echo "Base de donnees reinitialisee."
echo "Demarrez l'application pour charger les donnees de demonstration."
