
FROM maven:3.9.12-eclipse-temurin-25 AS dependencies
WORKDIR /opt/app

COPY pom.xml .
COPY ai-agent/pom.xml ai-agent/pom.xml
COPY bot/pom.xml bot/pom.xml
COPY build-report-aggregate/pom.xml build-report-aggregate/pom.xml
COPY common-proto/pom.xml common-proto/pom.xml
COPY load-service/pom.xml load-service/pom.xml
COPY scrapper/pom.xml scrapper/pom.xml

RUN mvn install -N -Drevision=0.0.1

RUN mvn dependency:go-offline -B -Drevision=0.0.1
FROM maven:3.9.12-eclipse-temurin-25 AS builder


WORKDIR /opt/app
COPY --from=dependencies /root/.m2 /root/.m2
COPY --from=dependencies /opt/app /opt/app
COPY ai-agent/src /opt/app/ai-agent/src
COPY bot/src /opt/app/bot/src
COPY common-proto/src /opt/app/common-proto/src
COPY load-service/src /opt/app/load-service/src
COPY scrapper/src /opt/app/scrapper/src
COPY . .
RUN mvn package -DskipTests -Drevision=0.0.1

FROM eclipse-temurin:25-jre-jammy
WORKDIR /opt/app
COPY --from=builder /opt/app/scrapper/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]

