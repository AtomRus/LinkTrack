FROM maven:3.9.12-eclipse-temurin-25 AS builder
WORKDIR /opt/app

COPY pom.xml .
COPY ai-agent/pom.xml ai-agent/pom.xml
COPY bot/pom.xml bot/pom.xml
COPY build-report-aggregate/pom.xml build-report-aggregate/pom.xml
COPY common-proto/pom.xml common-proto/pom.xml
COPY load-service/pom.xml load-service/pom.xml
COPY scrapper/pom.xml scrapper/pom.xml

RUN mvn install -N -Drevision=0.0.1

COPY ai-agent/src ai-agent/src
COPY bot/src bot/src
COPY common-proto/src common-proto/src
COPY load-service/src load-service/src
COPY scrapper/src scrapper/src

RUN mvn package -pl load-service -am -DskipTests -Drevision=0.0.1

FROM eclipse-temurin:25-jre-jammy
WORKDIR /opt/app
COPY --from=builder /opt/app/load-service/target/*.jar app.jar
EXPOSE 8090
ENTRYPOINT ["java", "-jar", "app.jar"]
