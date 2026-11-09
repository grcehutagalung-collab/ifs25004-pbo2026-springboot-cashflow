# Tahap 1: build (termasuk frontend Vaadin mode production)
FROM maven:3.9-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -B -q -DskipTests -Pproduction clean package

# Tahap 2: runtime ringan
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"
EXPOSE 8080
# Platform hosting biasanya memberi port lewat variabel PORT.
CMD ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]
