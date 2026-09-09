# ============================================================
# Etage 1 : build (Maven + JDK 17)
# ============================================================
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . .
RUN ./mvnw -q -DskipTests package

# ============================================================
# Etage 2 : runtime (JRE uniquement, image legere)
# ============================================================
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/inventory-api-1.0.0.jar app.jar

ENV DB_URL=jdbc:postgresql://db:5432/inventorydb \
    DB_USERNAME=inventory \
    DB_PASSWORD=inventory \
    LOW_STOCK_THRESHOLD=5

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
