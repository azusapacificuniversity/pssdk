# pssdk-example-lucee

A Lucee 6 application that talks to a PeopleSoft App Server via
[`edu.apu.pssdk`](https://github.com/azusapacificuniversity/pssdk). Exposes a
single REST endpoint that creates an advising note through the
**`SAA_ADV_NOTE`** Component Interface using `Create` + `Save` (and `Cancel`
on failure), with an OpenAPI 3 spec and a Swagger UI on top.

`pssdk` does not ship a Lucee binding; this project follows the structure of the
nodejs/express example in the SDK repo, replacing GraalNodeJs with CFML invoking
the same Java classes (`edu.apu.pssdk.AppServer` / `edu.apu.pssdk.CI`) directly.

## Layout

```
.
├── build.gradle              # shadow jar bundling pssdk + psjoa + logback
├── settings.gradle
├── Dockerfile                # FROM lucee/lucee:latest
├── docker-compose.yml
├── .env.sample
└── webroot/
    ├── Application.cfc
    ├── index.cfm                 # → /swagger/
    ├── openapi.cfm               # OpenAPI 3 spec
    ├── swagger/index.html        # Swagger UI (CDN)
    ├── api/v1/notes.cfm          # POST endpoint
    └── services/
        └── AdvisingNoteService.cfc
```

> [!NOTE]
>
> `com.oracle.peoplesoft:psjoa` is not published to Maven Central. Supply it
> yourself — a local Maven repository, a Gradle `flatDir`, or an artifact
> installed into `~/.m2` — and match the version to the PeopleTools release of
> your App Server.

## Build & run

```bash
# 1. populate .env from .env.sample >>> OR <<< set the env vars in docker-compose.yml directly
cp .env.sample .env && $EDITOR .env

# 2. build the shadow jar (needs PSJOA_JAR_VERSION in env)
export PSJOA_JAR_VERSION=8.62.07
./gradlew build

# 3. build + run the Lucee container
docker compose up --build
```

Open <http://localhost:8888/swagger/> to try the endpoint.

## Endpoint

`POST /api/v1/notes.cfm` — JSON body matches the documented advising-note shape:

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

`AdvisingNoteService.cfc` mirrors the JS example:

1. `edu.apu.pssdk.AppServer.fromEnv()` — reads `PS_APPSERVER_HOSTPORT`,
   `PS_APPSERVER_USERNAME`, `PS_APPSERVER_PASSWORD` (and optional
   `PS_APPSERVER_DOMAINPW`).
2. `appServer.ciFactory("SAA_ADV_NOTE")` — opens the JOA session and returns
   a `CI` wrapper.
3. `ci.create({EMPLID, INSTITUTION, SAA_NOTE_ID})` — populates the CREATEKEYS.
4. `ci.save(fullNote)` — sets the rest of the fields plus the `SAA_ADV_NOTEDTL`
   child rowset and saves to the App Server.
5. On exception, `ci.cancel()` invokes the CI's CANCEL operation. It does not
   release the JOA session, so `ci.close()` runs in a `finally` on both paths.

Lucee auto-converts CFML structs/arrays to `java.util.Map` / `java.util.List`
when invoking Java methods, so the JSON payload flows straight into
`CI.create` / `CI.save` without manual marshalling.

## Notes on the container

- The shadow jar lands in `/usr/local/tomcat/lib/` so the system classloader
  can load `psft.pt8.joa.*` and `edu.apu.pssdk.*` cleanly (Lucee's mappings
  classloader can be picky about JCA-style classes).
- `CATALINA_OPTS` sets `-DTM_ALLOW_NOTLS=Y` to match the express example; drop
  this if your jolt port uses TLS.
- The gradle wrapper jar is intentionally not committed — run `gradle wrapper
  --gradle-version 8.14.3` once if `./gradlew` is missing the wrapper jar.

## Example curl command

```bash
curl -i -X POST http://localhost:8888/api/v1/notes.cfm \
    -H 'Content-Type: application/json' \
    -d '{
      "EMPLID": "12345678",
      "SAA_NOTE_ID": "99999",
      "SAA_NOTE_TYPE": "ADVISING",
      "SAA_NOTE_SUBTYPE": "GENERAL",
      "ADVISOR_ID": "87654321",
      "SAA_NOTE_STATUS": "OP",
      "SAA_NOTE_SUBJ": "Spring registration",
      "SAA_ADV_NOTEDTL": "Met with student to plan Spring 2026 schedule.",
    }'
```
