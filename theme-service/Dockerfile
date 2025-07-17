# build stage
FROM openjdk:17-jdk-slim AS builder
LABEL authors="kym8821"
ARG COMMON_MODULE="common"
ARG CONFIG_MODULE="backend_config"
# copy settings
COPY gradlew /app/
COPY gradle /app/gradle
COPY build.gradle /app/
COPY docker/docker-settings.gradle /app/settings.gradle
# copy code, secrets, common modules
COPY theme-service /app/module
COPY ${COMMON_MODULE} /app/${COMMON_MODULE}
COPY ${CONFIG_MODULE} /app/${CONFIG_MODULE}
# build and copy jar file
WORKDIR /app
RUN chmod +x gradlew
RUN ./gradlew build --build-cache
RUN cp module/build/libs/module*.jar /app.jar

# runtime stage
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=builder ./app.jar app.jar
# install docker
RUN apt-get update && \
    apt-get install -y curl && \
    curl -fsSL https://get.docker.com | sh
ENTRYPOINT ["java", "-XX:-UseContainerSupport", "-jar", "app.jar"]