FROM eclipse-temurin:25-jdk AS builder
WORKDIR /builder

COPY gradlew settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle gradle
COPY src src
# The cache mount keeps the Gradle wrapper and dependency jars between builds.
RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar --no-daemon \
    && java -Djarmode=tools -jar build/libs/app.jar extract --layers --destination extracted

# Throwaway keys for the AOT cache training run only. They never reach the runtime image.
RUN mkdir training \
    && openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out training/app.key \
    && openssl rsa -pubout -in training/app.key -out training/app.pub


# JRE

FROM eclipse-temurin:25-jre
RUN useradd --system --no-create-home runtomo
WORKDIR /application

COPY --from=builder /builder/extracted/dependencies/ ./
COPY --from=builder /builder/extracted/spring-boot-loader/ ./
COPY --from=builder /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder /builder/extracted/application/ ./

ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75"

# AOT cache training doesn't need migration.
RUN --mount=type=bind,from=builder,source=/builder/training,target=/training \
    POSTGRES_URL=jdbc:postgresql://localhost/training \
    POSTGRES_USER=training \
    POSTGRES_PASSWORD=training \
    GOOGLE_WEB_CLIENT_ID=training \
    JWT_PUBLIC_KEY=file:/training/app.pub \
    JWT_PRIVATE_KEY=file:/training/app.key \
    java -XX:AOTCacheOutput=app.aot -Dspring.context.exit=onRefresh -Dspring.flyway.enabled=false -jar app.jar

USER runtomo
EXPOSE 8080
ENTRYPOINT ["java", "-XX:AOTCache=app.aot", "-jar", "app.jar"]
