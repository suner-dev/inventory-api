#!/bin/bash
# ============================================================
# Script de demarrage rapide de l'API Inventory
# Usage : ./start.sh
# ============================================================

echo "Demarrage de l'API Inventory..."
echo "Swagger UI : http://localhost:8080/swagger-ui/index.html"
echo ""
echo "Appuyez sur Ctrl+C pour arreter."
echo ""

java -jar target/inventory-api-1.0.0.jar
