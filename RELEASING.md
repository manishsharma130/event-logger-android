# Releasing

Coordinate: `io.github.manishsharma130:events-logger` (version in `gradle.properties` → `VERSION_NAME`).

## 1. Bump the version

Set `VERSION_NAME` in `gradle.properties` and add a section in `CHANGELOG.md`.

## 2. Verify

```bash
./gradlew :events-logger:testReleaseUnitTest
./gradlew :events-logger:publishToMavenLocal
```

Install check: `~/.m2/repository/io/github/manishsharma130/events-logger/`

## 3. Create the Maven Central bundle

The task builds the release publication, signs every artifact, generates checksums, and packages the Maven repository layout into one ZIP:

```bash
./gradlew :events-logger:centralBundle
```

The uploadable file is created at:

```text
events-logger/build/distributions/events-logger-<version>-central-bundle.zip
```

## 4. Upload and publish

1. Sign in to the [Maven Central Publisher Portal](https://central.sonatype.com/publishing).
2. Open **Publish → Deployments** and upload the generated ZIP.
3. Wait for validation to finish.
4. Review the validated deployment and select **Publish**.

Published versions are immutable. If a release needs a correction, increment `VERSION_NAME` and create a new bundle.
