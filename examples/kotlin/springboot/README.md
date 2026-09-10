# pssdk-example-kotlin

A Kotlin + Spring Boot application that talks to a PeopleSoft App Server via
[`edu.apu.pssdk`](https://github.com/azusapacificuniversity/pssdk). Exposes a
single REST endpoint that creates an advising note through the
**`SAA_ADV_NOTE`** Component Interface using `Create` + `Save` (and `Cancel`
on failure), with an OpenAPI 3 spec and a Swagger UI on top.

`pssdk` does not ship a Kotlin binding; this project follows the structure of the
nodejs/express example in the SDK repo, replacing GraalNodeJs with Kotlin invoking
the same Java classes (`edu.apu.pssdk.AppServer` / `edu.apu.pssdk.CI`) directly.
It uses `pssdk` `4.0.0`, whose `Is.listOfStringToObjectMaps` check is what lets a plain
`java.util.List<Map<String, Object>>` — a Kotlin `mutableListOf(mutableMapOf(...))` —
populate a CI scroll without going through GraalVM polyglot proxies.

## Layout

```
.
├── build.gradle.kts          # Spring Boot fat jar bundling pssdk + psjoa
├── settings.gradle.kts
├── Dockerfile                # FROM eclipse-temurin:21-jre
├── docker-compose.yml
├── .env.sample
└── src/main/
    ├── kotlin/edu/apu/pssdk/example/
    │   ├── Application.kt              # entry point, OpenAPI info, / → Swagger UI
    │   ├── AdvisingNote.kt             # request/response DTOs + CI payload mapping
    │   ├── AdvisingNoteService.kt      # AppServer / CI calls
    │   ├── AdvisingNoteController.kt   # POST /api/v1/notes
    │   └── ApiExceptionHandler.kt      # JSON error bodies
    └── resources/application.yml
```

> [!NOTE]
>
> `com.oracle.peoplesoft:psjoa` is not published to Maven Central. Supply it
> yourself — a local Maven repository, a Gradle `flatDir`, or an artifact
> installed into `~/.m2` — and match the version to the PeopleTools release of
> your App Server.

## Build & run

```bash
# 1. build the fat jar (needs PSJOA_JAR_VERSION in env)
export PSJOA_JAR_VERSION=8.62.07
./gradlew build

# 2. run it — App Server credentials come from the environment
export PS_APPSERVER_HOSTPORT=appserver-host.example.edu:9000
export PS_APPSERVER_USERNAME=PS_API_USER
export PS_APPSERVER_PASSWORD=...
java -jar build/libs/pssdk-example-kotlin.jar

# ...or in a container
cp .env.sample .env && $EDITOR .env
docker compose up --build
```

Open <http://localhost:8080/swagger-ui.html> to try the endpoint. The generated
spec is at <http://localhost:8080/v3/api-docs>.

## Endpoint

`POST /api/v1/notes` — JSON body matches the documented advising-note shape:

```json
{
  "EMPLID": "12345678",
  "SAA_NOTE_SUBJ": "Spring registration",
  "SAA_ADV_NOTEDTL": "Met with student to plan Spring 2026 schedule.",
  "ADVISOR_ID": "87654321",
  "SCC_ROW_ADD_OPRID": "advisor.netid"
}
```

Defaults match the express example (`INSTITUTION=APU`, `SAA_NOTE_TYPE=ADVISING`,
`SAA_NOTE_SUBTYPE=GENERAL`, `SAA_NOTE_STATUS=OP`, `SAA_NOTE_ID=99999`).

## How the CI flow works

`AdvisingNoteService.kt` mirrors the JS example:

1. `edu.apu.pssdk.AppServer.fromEnv()` — reads `PS_APPSERVER_HOSTPORT`,
   `PS_APPSERVER_USERNAME`, `PS_APPSERVER_PASSWORD` (and optional
   `PS_APPSERVER_DOMAINPW`). Called lazily, so the app still starts (and Swagger
   UI still works) without App Server credentials.
2. `appServer.ciFactory("SAA_ADV_NOTE")` — opens the JOA session and returns
   a `CI` wrapper.
3. `ci.create({EMPLID, INSTITUTION, SAA_NOTE_ID})` — populates the CREATEKEYS.
4. `ci.save(fullNote)` — sets the rest of the fields plus the `SAA_ADV_NOTEDTL`
   child rowset and saves to the App Server.
5. `ci.toProxyObject().getMember("SAA_NOTE_ID")` — PeopleSoft auto-numbers the note
   on Save when the CREATEKEY is the `99999` placeholder, so the assigned ID is read
   back off the CI (before the session closes) and returned in the response.
6. `ci.close()` on every path, via Kotlin's `use { }` — `CI` implements `Closeable`
   and `close()` is what disconnects the session. `ci.cancel()` runs first on the
   exception path, but it only invokes the CI's CANCEL operation and does not
   release the session, so it always needs a `close()` after it.

Kotlin maps and lists *are* `java.util.Map` / `java.util.List`, so the payload
flows straight into `CI.create` / `CI.save` without manual marshalling. Immutable
collections (`mapOf` / `listOf`) are fine: `CiScroll.populateWith` copies the
incoming list before removing matched rows from it.

## Notes

- `toProxyObject()` is the SDK's only way to read data out of a CI, and it returns a
  Graal `ProxyObject`, so `org.graalvm.polyglot:polyglot` is declared explicitly —
  pssdk keeps it runtime-scoped, which leaves it off the compile classpath.
- The `AppServer` instance is a lazy singleton so its `PropertyInfoCatalog` cache
  is reused; each request still gets its own JOA session from `ciFactory`.
- Jackson's Kotlin module is `tools.jackson.module:jackson-module-kotlin` —
  Spring Boot 4 is on Jackson 3, and the Jackson 2 artifact is silently inert
  (data-class defaults never get applied).
- `@JsonAutoDetect(fieldVisibility = ANY, getterVisibility = NONE)` on the DTOs
  keeps the PeopleSoft field names intact: Kotlin generates `getEMPLID()`, which
  Jackson would otherwise name `emplid` and swagger-core would publish as such.
- `TM_ALLOW_NOTLS=Y` matches the express example; drop it if your jolt port uses TLS.

## Example curl command

```bash
curl -i -X POST http://localhost:8080/api/v1/notes \
    -H 'Content-Type: application/json' \
    -d '{
      "EMPLID": "12345678",
      "SAA_NOTE_ID": "99999",
      "SAA_NOTE_TYPE": "ADVISING",
      "SAA_NOTE_SUBTYPE": "GENERAL",
      "ADVISOR_ID": "87654321",
      "SAA_NOTE_STATUS": "OP",
      "SAA_NOTE_SUBJ": "Spring registration",
      "SAA_ADV_NOTEDTL": "Met with student to plan Spring 2026 schedule."
    }'
```
