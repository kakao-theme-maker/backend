FROM eclipse-temurin:21-jdk

ENV ANDROID_HOME=/opt/android-sdk
ENV PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools

RUN apt-get update && apt-get install -y \
    wget \
    unzip \
    && rm -rf /var/lib/apt/lists/*

# Android Command Line Tools 설치
RUN mkdir -p $ANDROID_HOME/cmdline-tools

RUN wget https://dl.google.com/android/repository/commandlinetools-linux-13114758_latest.zip \
    -O cmdline-tools.zip && \
    unzip cmdline-tools.zip && \
    rm cmdline-tools.zip && \
    mv cmdline-tools $ANDROID_HOME/cmdline-tools/latest

# 라이선스 동의
RUN yes | sdkmanager --licenses

# SDK 설치
RUN sdkmanager \
    "platform-tools" \
    "platforms;android-35" \
    "build-tools;35.0.0"

WORKDIR /source

ENTRYPOINT ["./gradlew"]