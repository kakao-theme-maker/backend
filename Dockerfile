FROM eclipse-temurin:17-jre-jammy

LABEL authors="kym8821"

WORKDIR /app

COPY build/libs/kakao-theme-maker.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
