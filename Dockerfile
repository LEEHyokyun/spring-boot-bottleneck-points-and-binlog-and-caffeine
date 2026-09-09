## BUILD

FROM eclipse-temurin:21-jdk AS build

WORKDIR /app
COPY . .
RUN ./gradlew clean bootJar

## DEPLOY

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=build /app/build/libs/spring-boot-bottleneck-points-and-binlog-and-caffeine.jar spring-boot-bottleneck-points-and-binlog-and-caffeine.jar

VOLUME /tmp

ENTRYPOINT ["java","-jar","spring-boot-bottleneck-points-and-binlog-and-caffeine.jar"]