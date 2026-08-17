# =========================
# Stage 1: Builder
# =========================
FROM eclipse-temurin:25-jdk AS builder

# Install Maven
RUN apt-get update && apt-get install -y maven


WORKDIR /build

# Copy only pom.xml first (to cache dependencies)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy source code
COPY src ./src

# Build project
RUN mvn clean package -DskipTests

# =========================
# Stage 2: Runtime
# =========================
FROM eclipse-temurin:25-jdk

WORKDIR /app

COPY --from=builder /build/target/customers-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8091
ENTRYPOINT ["java", "-jar", "app.jar"]



