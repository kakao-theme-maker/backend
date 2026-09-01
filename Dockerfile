FROM eclipse-temurin:17-jre-jammy

LABEL authors="kym8821"

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends docker.io \
    && rm -rf /var/lib/apt/lists/* \

COPY build/libs/kakao-theme-maker.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
