# gateway Dockerfile
FROM eclipse-temurin:17-jdk-alpine

LABEL maintainer="ecommerce-platform"
LABEL description="智能电商运营平台 - API网关"

WORKDIR /app

COPY target/gateway-1.0.0-SNAPSHOT.jar app.jar

ENV JAVA_OPTS="-Xms128m -Xmx256m -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
