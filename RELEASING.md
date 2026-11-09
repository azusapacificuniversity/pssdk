# Releasing pssdk

A release publishes two artifacts from this repository:

- the Java library to Maven Central as `edu.apu.pssdk:pssdk`
- the Node.js wrapper in `lib/npm/` to npm as `pssdk`

The Maven and npm versions always match: both artifacts are released together
under the same version, even when only one of them changed.

Maven Central only accepts signed artifacts, so you need the signing key and
Central credentials set up locally before releasing.

## Prerequisites (once per machine)

1. **Java 17.** Gradle downloads it through its toolchain support if missing.
2. **GPG signing key.** Import the INTDEV key from Vault `intdev/pgp`:

   ```
   gpg --import <private-key-file>
   gpg --list-secret-keys --keyid-format=long
   ```

   The key's uid is `Azusa Pacific University - INTDEV <intdev@apu.edu>`.
   Check its expiry date. To extend it, run `gpg --edit-key <key-id>`, use
   `expire`, then upload the public key again to `keys.openpgp.org`.
3. **Gradle credentials.** Add these to `~/.gradle/gradle.properties` (never
   to the repository). The token comes from Vault
   `intdev/central.sonatype.com/token`:

   ```
   mavenCentralUsername=<token-username>
   mavenCentralPassword=<token-password>
   signing.gnupg.keyName=<key-id-from-gpg>
   ```
4. **npm.** Log in as an owner of the package (`npm owner ls pssdk`) with
   `npm login`, and check with `npm whoami`.
5. **Check signing works.** `./gradlew publishToMavenLocal` should finish and
   leave `.asc` signature files in `~/.m2/repository/edu/apu/pssdk/pssdk/`.

## Steps

1. **Pick the version** from the commits since the last tag
   (`git log vA.B.C..origin/main`):
   - major: breaking API changes
   - minor: new features
   - patch: fixes and dependency updates only
2. **Create the release branch:**

   ```
   git fetch origin
   git switch -c release/vX.Y.Z origin/main
   ```
3. **Bump the version** to `X.Y.Z` in `build.gradle` and
   `lib/npm/package.json`. In `lib/npm`, run `npm i && npm audit`; if it
   reports a vulnerable dependency, raise that dependency's minimum version in
   `package.json` to the fixed one.
4. **Update `CHANGELOG.md`:** add a `## [X.Y.Z] - YYYY-MM-DD` section and its
   compare link,
   `[X.Y.Z]: https://github.com/azusapacificuniversity/pssdk/compare/vA.B.C...vX.Y.Z`.
5. **Build and test:** `./gradlew clean build`.
6. **Commit and tag.** Commit as `Release vX.Y.Z` and create a lightweight
   tag on it, then push both:

   ```
   git tag vX.Y.Z
   git push -u origin release/vX.Y.Z
   git push origin vX.Y.Z
   ```
7. **Open a PR** titled `Release vX.Y.Z` from `release/vX.Y.Z` into `main`,
   and merge it.
8. **Publish from the tag:**

   ```
   git switch --detach vX.Y.Z
   ./gradlew publishAndReleaseToMavenCentral
   cd lib/npm && npm publish
   ```

   For a pre-release, publish to npm with `--tag rc` or `--tag alpha` so it
   doesn't become `latest`.
9. **Create the GitHub release** `vX.Y.Z` from the tag, using the CHANGELOG
   section as the release notes.
10. **Start the next development version.** On an up-to-date `main`, bump the
    patch number in `build.gradle` only to a snapshot, for example
    `4.0.1` to `version = '4.0.2-SNAPSHOT'`, and commit it as
    `Prepare 4.0.2-SNAPSHOT`. Don't tag it or publish it.
    `lib/npm/package.json` stays at the released version.

## Verify

- `npm view pssdk version` shows `X.Y.Z`.
- https://central.sonatype.com/artifact/edu.apu.pssdk/pssdk shows `X.Y.Z`.
  It can take about 30 minutes to appear.

## Troubleshooting

- `gpg: signing failed: Inappropriate ioctl for device`: run
  `export GPG_TTY=$(tty)` and try again.
- `401` from Maven Central: generate a new user token on
  central.sonatype.com, then update `~/.gradle/gradle.properties` and Vault.
- Expired key: extend its expiry (prerequisite 2) before publishing.
