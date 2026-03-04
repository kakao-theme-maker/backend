# build stage
FROM eclipse-temurin:17-jdk AS builder
LABEL authors="kym8821"
ARG CONFIG_MODULE="backend_config"
# copy settings
WORKDIR /app
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
RUN chmod +x gradlew
# 의존성 캐싱
RUN ./gradlew dependencies --no-daemon
# 실제 소스코드와 설정파일 복사
COPY src src
COPY ${CONFIG_MODULE} ${CONFIG_MODULE}
# build and copy jar file
RUN ./gradlew build --no-daemon
RUN cp build/libs/kakao-theme-maker.jar /app.jar

# runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
# install docker
RUN apk add --no-cache docker-cli
# run backend container
COPY --from=builder ./app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]