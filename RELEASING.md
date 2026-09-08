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

## 3. Publish remotely

Set a repository URL (GitHub Packages, Nexus, or Maven Central via Sonatype):

```properties
PUBLISH_URL=https://maven.pkg.github.com/OWNER/eventLogger
PUBLISH_USER=your-github-username
PUBLISH_PASSWORD=your-github-token
```

Or pass them as environment variables `PUBLISH_USER` / `PUBLISH_PASSWORD` (`GITHUB_TOKEN` is also accepted as the password).

```bash
./gradlew :events-logger:publish
```

Maven Central additionally requires a registered group id (`com.eventlogger` or a domain you own), signing, and a Sonatype account. Configure that in `PUBLISH_URL` when you have it.
