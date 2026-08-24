# ============================================================
# Multi-stage build — Frontend (Node) + Backend (Maven/JRE)
# ============================================================

# --- Stage 1: build do frontend ---
FROM node:22-alpine AS frontend-build
WORKDIR /app/frontend
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# --- Stage 2: build do backend ---
FROM eclipse-temurin:21-jdk-alpine AS backend-build
WORKDIR /app/backend
COPY backend/.mvn .mvn
COPY backend/mvnw backend/pom.xml ./
# cache de dependências
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B
COPY backend/src ./src
# copia o build do frontend para static resources do Spring Boot
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static
RUN ./mvnw package -DskipTests -B

# --- Stage 3: runtime ---
FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

# Diretório de documentos parametrizável via variável de ambiente
ENV DOCUMENTOS_DIR=/app/documentos

COPY --from=backend-build /app/backend/target/*.jar app.jar

# Cria o diretório de documentos (vazio — será populado via volume ou Object Storage)
RUN mkdir -p ${DOCUMENTOS_DIR}

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
