# PSSDK-Flask, an example with Flask and GraalPy

PSSDK Example with Flask and GraalPy. Exposes a single REST endpoint that reads
a user profile through the **`USER_PROFILE`** Component Interface.

## PSJOA_JAR_VERSION build env var

The build packages the PSJOA jar file along with PSSDK. A different version of
PSJOA is required for each PeopleSoft environment. The version of the PSJOA jar
file should match the version of PeopleTools in your App Server, and you should
probably grab the jar file from there.

> [!NOTE]
>
> `com.oracle.peoplesoft:psjoa` is not published to Maven Central. Supply it
> yourself — a local Maven repository, a Gradle `flatDir`, or an artifact
> installed into `~/.m2` — and match the version to the PeopleTools release of
> your App Server.

## Build and Run with Docker Compose

```
PSJOA_JAR_VERSION=8.60.20 ./gradlew --refresh-dependencies build && docker compose up --build
```

The App Server connection is read from `.env` by `AppServer.fromEnv()`:

```
PS_APPSERVER_HOSTPORT
PS_APPSERVER_DOMAINPW
PS_APPSERVER_USERNAME
PS_APPSERVER_PASSWORD
```

Then:

```
curl http://localhost:5000/api/v1/profiles/<userid>
```
