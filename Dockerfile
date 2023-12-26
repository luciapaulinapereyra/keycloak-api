FROM maven:3.8.5-openjdk-17-slim

WORKDIR /app

COPY . .

RUN mvn clean package -DskipTests

ARG PROFILE
ENV ENV_PROFILE=$PROFILE

CMD ["java", "-jar","-Dspring.profiles.active=${ENV_PROFILE}", "target/keycloak-api-0.0.1-SNAPSHOT.jar"]
