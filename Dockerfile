FROM eclipse-temurin:25-jdk AS build
WORKDIR /app
COPY gradle gradle
COPY build.gradle settings.gradle ./
ADD https://services.gradle.org/distributions/gradle-9.3.1-bin.zip /tmp/gradle.zip
RUN jar xf /tmp/gradle.zip -C /opt && rm /tmp/gradle.zip && chmod +x /opt/gradle-9.3.1/bin/gradle
RUN /opt/gradle-9.3.1/bin/gradle wrapper --no-daemon
RUN ./gradlew dependencies --no-daemon || true
COPY src src
RUN ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:25-jre-alpine
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
