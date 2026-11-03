# ─────────────────────────────────────────────────────────────────────────────
# Stage 1 : Build – compile le projet Maven et génère le JAR
# ─────────────────────────────────────────────────────────────────────────────
FROM maven:3.9.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copier seulement le POM d'abord pour profiter du cache Docker (layers)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copier le reste du code source
COPY src ./src

# Compiler et packager (skip tests ici – les tests tournent dans Jenkins)
RUN mvn clean package -DskipTests -B

# ─────────────────────────────────────────────────────────────────────────────
# Stage 2 : Runtime – image légère avec uniquement le JAR
# ─────────────────────────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine

# Métadonnées de l'image
LABEL maintainer="MiniDesk Team" \
      app="minidesk-backend" \
      version="1.0"

WORKDIR /app

# Créer un utilisateur non-root pour la sécurité
RUN addgroup -S minidesk && adduser -S minidesk -G minidesk

# Copier le JAR depuis le stage builder
COPY --from=builder /app/target/*.jar app.jar

# Changer le propriétaire
RUN chown minidesk:minidesk app.jar

USER minidesk

# Port exposé par Spring Boot
EXPOSE 8081

# Point d'entrée avec options JVM pour conteneur (mémoire adaptative)
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
