FROM eclipse-temurin:21-jre
LABEL maintainer="Azusa Pacific University"

RUN useradd -m -g root app

# Spring Boot fat jar, with pssdk + psjoa + logback inside (see build.gradle.kts).
COPY build/libs/pssdk-example-kotlin.jar /opt/pssdk/

EXPOSE 8080

USER app

ENTRYPOINT ["java", "-jar", "/opt/pssdk/pssdk-example-kotlin.jar"]
