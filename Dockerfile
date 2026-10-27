FROM node:22-alpine AS frontend-build

WORKDIR /app/frontend

COPY frontend/package*.json ./
RUN npm ci

COPY frontend/ ./
RUN npm run build


FROM maven:3.9-eclipse-temurin-21-alpine AS backend-build

WORKDIR /app

COPY backend/pom.xml ./
RUN mvn -q -DskipTests dependency:go-offline

COPY backend/src ./src
COPY --from=frontend-build /app/frontend/dist ./src/main/resources/static

RUN mvn -DskipTests package


FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=backend-build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]