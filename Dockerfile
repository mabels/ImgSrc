#FROM maven:3-jdk-8-alpine
FROM library/openjdk:23-slim as builder

ENV LC_CTYPE en_US.UTF-8
WORKDIR /app
COPY pom.xml /app/build/
COPY src /app/build/src
#COPY alpn-boot-8.1.11.v20170118.jar /app/
#COPY $HOME/.m2/repository/org/mortbay/jetty/alpn/alpn-boot/8.1.11.v20170118/alpn-boot-8.1.11.v20170118.jar /app

RUN apt-get update -y && \
    apt-get install -y maven && \
    (cd /app/build && mvn package && cp target/imgsrc*.jar /app/imgsrc.jar) 

FROM library/openjdk:23-slim

RUN apt-get update -y && \
    apt-get install -y fonts-dejavu

COPY --from=builder /app/imgsrc.jar /app/imgsrc.jar

#CMD ["java", "-Xbootclasspath/p:/app/alpn-boot-8.1.11.v20170118.jar", "-jar", "/app/imgsrc.jar", "-ssl", "https"]
CMD ["java", "-jar", "/app/imgsrc.jar", "-ssl", "https"]

