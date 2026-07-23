# ai-service Dockerfile
FROM eclipse-temurin:17-jdk-alpine

LABEL maintainer="ecommerce-platform"
LABEL description="智能电商运营平台 - AI服务"

WORKDIR /app

COPY target/ai-service-1.0.0-SNAPSHOT.jar app.jar

ENV JAVA_OPTS="-Xms512m -Xmx1g -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
