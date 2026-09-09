# API de Gestion d'un Inventaire de Produits

[![Java](https://img.shields.io/badge/Java-17-important)](https://adoptium.net) ![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4-brightgreen) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)

## Description

API REST de gestion d'un inventaire de produits avec **suivi des stocks**. Elle permet de créer, lire, modifier et supprimer des produits, et d'**alerter automatiquement lorsque le stock devient faible** (< 5 unités).

**Fonctionnalités :**
- CRUD complet des produits (nom, prix en BigDecimal, quantite en stock)
- Detection automatique du stock faible (< 5 unites)
- Persistance PostgreSQL via Spring Data JPA
- Documentation Swagger/OpenAPI interactive
- Donnees de demonstration chargees automatiquement

## Technologies

- Java 17
- Spring Boot 3.4 (Web, Data JPA, Validation)
- Spring Data JPA / Hibernate
- PostgreSQL 16
- Flyway (migrations SQL versionnées + seed)
- Lombok
- Springdoc OpenAPI (Swagger UI)
- Bean Validation (Jakarta)
- JUnit 5, Mockito, MockMvc

## Architecture

```
ProductController → ProductService (ProductServiceImpl) → ProductRepository → PostgreSQL
```

- Le **Controller** gère HTTP, le **Service** porte la logique métier (dont le seuil de stock faible), le **Repository** interroge la base.
- Les **DTOs** (jamais les entités JPA) sont exposés ; le prix est un **BigDecimal**.

## Prérequis

- JDK 17 ou supérieur
- Maven (wrapper `./mvnw` inclus)
- Docker (optionnel : démo en une commande) **ou** PostgreSQL 16 installé

## Configuration PostgreSQL

Base, utilisateur et mot de passe (adaptez) :

```sql
CREATE DATABASE inventorydb;
CREATE USER inventory WITH PASSWORD 'inventory';
GRANT ALL PRIVILEGES ON DATABASE inventorydb TO inventory;
```

Les tables et données de démonstration sont créées automatiquement au premier démarrage par **Flyway** (`V1__init.sql` schéma, `V2__seed.sql` 10 produits dont des stocks faibles). Aucun secret n'est dans le code :

| Variable | Défaut | Rôle |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://localhost:5432/inventorydb` | URL JDBC |
| `DB_USERNAME` | `inventory` | Utilisateur |
| `DB_PASSWORD` | `inventory` | Mot de passe |
| `LOW_STOCK_THRESHOLD` | `5` | Seuil de stock faible (règle métier) |
| `SERVER_PORT` | `8080` | Port HTTP |

## Installation

```bash
git clone https://github.com/suner-dev/inventory-api.git
cd inventory-api
```

## Lancement

### Methode 1 — Docker (zero-config, recommandee)

Une seule commande demarre PostgreSQL + l'API + insere automatiquement les donnees de demonstration (Flyway). Aucune configuration requise :

```bash
docker compose up --build
```

Puis ouvrir **http://localhost:8080/swagger-ui/index.html**.

> Le schema (V1) et les 10 produits de demo (V2) sont crees automatiquement au premier demarrage. Les donnees persistent dans le volume Docker `inventory-pgdata`.

### Methode 2 — Local (si PostgreSQL est deja installe)

```bash
# 1. Creer la base et l'utilisateur (une seule fois)
#    psql -U postgres -c "CREATE DATABASE inventorydb;"
#    psql -U postgres -c "CREATE USER inventory WITH PASSWORD 'inventory';"
#    psql -U postgres -c "GRANT ALL PRIVILEGES ON DATABASE inventorydb TO inventory;"

# 2. Lancer l'API (le schema + le seed sont automatiques via Flyway)
./mvnw spring-boot:run
```

Si le port 8080 est occupe : `SERVER_PORT=8081 ./mvnw spring-boot:run`.

### Methode 3 — Script de demarrage rapide

```bash
# Compiler et demarrer
./start.sh
```

### Reinitialisation de la base de donnees

Si vous voulez recommencer avec des donnees fraiches :

```bash
./reset-db.sh
```

## Swagger

- Swagger UI : **http://localhost:8080/swagger-ui/index.html**
- OpenAPI JSON : http://localhost:8080/v3/api-docs

Toute l'API est testable depuis Swagger UI (exemples déjà remplis sur chaque endpoint).

## Endpoints

| Méthode | Endpoint | Description | Statut |
|---|---|---|---|
| POST | `/api/products` | Créer un produit | 201 / 400 |
| GET | `/api/products` | Lister tous les produits (`?lowStock=true` pour filtrer) | 200 |
| GET | `/api/products/low-stock` | Produits en stock faible (< 5) | 200 |
| GET | `/api/products/{id}` | Récupérer un produit | 200 / 404 |
| PUT | `/api/products/{id}` | Modifier un produit | 200 / 400 / 404 |
| DELETE | `/api/products/{id}` | Supprimer un produit | 204 / 404 |

## Exemples de requêtes

Créer un produit :

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Laptop","price":750000.00,"stockQuantity":10}'
```

```json
{ "id": 1, "name": "Laptop", "price": 750000.00, "stockQuantity": 10, "lowStock": false, "createdAt": "2026-09-09T15:30:00", "updatedAt": null }
```

## Gestion du stock faible

Un produit est en **stock faible** strictement en dessous du seuil (**5** par défaut, configurable) :

| Stock | Low stock ? |
|---|---|
| 0, 1, 2, 3, 4 | ✅ **oui** |
| 5, 6, … | ❌ non |

- L'endpoint `GET /api/products/low-stock` effectue le filtrage **dans la base** (`findByStockQuantityLessThan`).
- Chaque réponse produit expose un indicateur **`lowStock`** pour un repérage immédiat.

## Validation

- `name` : obligatoire, non vide, ≤ 150 caractères.
- `price` : obligatoire, **strictement positif**, `BigDecimal` (jamais `double`).
- `stockQuantity` : obligatoire, **≥ 0** (stock négatif interdit).

## Gestion des erreurs

| Code | Situation |
|---|---|
| `400` | Données invalides (nom vide, prix ≤ 0, stock < 0, JSON malformé) |
| `404` | Produit inexistant |
| `500` | Erreur serveur imprévue (message générique, aucune stack trace) |

## Tests

```bash
./mvnw test
```

29 tests, dont 18 tests d'**intégration sur PostgreSQL réel** : scénario complet en 8 étapes (création → liste → lecture → modification stock 3 → low-stock → stock 5 → suppression → 404), validation (nom vide, prix nul/négatif, stock négatif, JSON malformé), et cas limites du seuil (0/1/4 faible, 5/6 normal).
