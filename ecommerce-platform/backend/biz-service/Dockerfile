# biz-service Dockerfile
FROM eclipse-temurin:17-jdk-alpine

LABEL maintainer="ecommerce-platform"
LABEL description="智能电商运营平台 - 业务服务"

WORKDIR /app

# 复制jar包（需要先执行 mvn clean package 生成）
COPY target/biz-service-1.0.0-SNAPSHOT.jar app.jar

# JVM参数优化（容器环境）
ENV JAVA_OPTS="-Xms256m -Xmx512m -Djava.security.egd=file:/dev/./urandom"
ENV SPRING_PROFILES_ACTIVE=docker

EXPOSE 8082

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
