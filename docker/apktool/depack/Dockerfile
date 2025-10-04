FROM openjdk:21-jdk-slim

# install wget
RUN apt-get update && apt-get install -y wget

# install apktool
RUN wget https://bitbucket.org/iBotPeaches/apktool/downloads/apktool_2.11.1.jar -O /usr/local/bin/apktool.jar && \
    wget https://raw.githubusercontent.com/iBotPeaches/Apktool/master/scripts/linux/apktool -O /usr/local/bin/apktool && \
    chmod +x /usr/local/bin/apktool

RUN mkdir /input /output

# use entrypoint properly
ENTRYPOINT sh -c "ls -la /input && apktool -f d /input/*.apk -o /output"
