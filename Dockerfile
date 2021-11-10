#FROM maven:3-jdk-8-alpine
FROM library/openjdk:8-alpine

ENV LC_CTYPE en_US.UTF-8
WORKDIR /app
COPY pom.xml /app/build/
COPY src /app/build/src
#COPY alpn-boot-8.1.11.v20170118.jar /app/
#COPY $HOME/.m2/repository/org/mortbay/jetty/alpn/alpn-boot/8.1.11.v20170118/alpn-boot-8.1.11.v20170118.jar /app

RUN apk update && \
    apk add maven && \
    apk add ttf-dejavu && \
    (cd /app/build && mvn package -no-transfer-progress && cp target/imgsrc*.jar /app/imgsrc.jar) && \
    rm -rf /app/build $HOME/.m2

#COPY cert.pem chain.pem fullchain.pem privkey.pem /app/


#CMD ["java", "-Xbootclasspath/p:/app/alpn-boot-8.1.11.v20170118.jar", "-jar", "/app/imgsrc.jar", "-ssl", "https"]
CMD ["java", "-jar", "/app/imgsrc.jar", "-ssl", "https"]

