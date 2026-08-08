# Backend build stage
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY maven-settings.xml /root/.m2/settings.xml
COPY pom.xml .
COPY src ./src
ENV MAVEN_OPTS="-Dmaven.wagon.http.retryHandler.count=5 -Dmaven.wagon.http.connectionTimeout=60000 -Dmaven.wagon.http.readTimeout=120000"
RUN mvn -B -DskipTests package

# Backend runtime stage
FROM eclipse-temurin:21-jre-alpine
RUN apk add --no-cache curl tzdata
WORKDIR /app
COPY --from=build /build/target/SpringBoot-HouseMarket-0.0.1-SNAPSHOT.jar /app/app.jar
RUN mkdir -p /app/uploads
COPY uploads /app/uploads
ENV TZ=Asia/Shanghai
EXPOSE 8082
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
