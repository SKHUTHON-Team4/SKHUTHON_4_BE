FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY build/libs/skhuthon4-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]