# PSSDK-Express, an example with Express and GraalNodejs

PSSDK Example with Express and GraalNodejs.

## PSJOA_JAR_VERSION build env var

The build packages the PSJOA jar file along with PSSDK. Different version of
PSJOA is required for each PeopleSoft environment. The version of the PSJOA
jar file should match the version of PeopleTools in your App Server, and you
should probably grab the jar file from there.

> [!NOTE]
>
> `com.oracle.peoplesoft:psjoa` is not published to Maven Central. Supply it
> yourself — a local Maven repository, a Gradle `flatDir`, or an artifact
> installed into `~/.m2` — and match the version to the PeopleTools release of
> your App Server.

## Build and Run with Docker

To build a docker container locally

```
PSJOA_JAR_VERSION=8.60.20 ./gradlew --refresh-dependencies build && \
docker build -t pssdk-express:latest .
```

This will build the docker image for pssdk-express from the local dockerfile.

```
docker run -d -p 3000:3000 --env-file ./.env pssdk-sapi:latest
```

## Build and Run with Docker Compose

```
PSJOA_JAR_VERSION=8.60.20 ./gradlew --refresh-dependencies build && docker compose up --build
```
