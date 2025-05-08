FROM openjdk:21-jdk-slim

# install package for unpackage, repackage
RUN apt-get -y update && \
    apt-get install -y wget apksigner zipalign

ENV PATH="${PATH}:/usr/lib/android-sdk/build-tools"

# install apktool
RUN wget https://bitbucket.org/iBotPeaches/apktool/downloads/apktool_2.11.1.jar -O apktool.jar && \
    wget https://raw.githubusercontent.com/iBotPeaches/Apktool/master/scripts/linux/apktool && \
    chmod +x apktool

COPY ./key.jks /key.jks

COPY ./repacker.sh /repacker.sh

ENTRYPOINT sh -c "./repacker.sh"

