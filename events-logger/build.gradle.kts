import java.security.MessageDigest

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.dokka.javadoc)
    `maven-publish`
    signing
}

group = providers.gradleProperty("GROUP").get()
version = providers.gradleProperty("VERSION_NAME").get()

android {
    namespace = "com.eventlogger.events_logger"
    compileSdk = 36

    defaultConfig {
        minSdk = 21
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20250107")
}

val dokkaJavadocJar by tasks.registering(Jar::class) {
    description = "Creates a Javadoc JAR using Dokka"
    from(tasks.dokkaGeneratePublicationJavadoc.flatMap { it.outputDirectory })
    archiveClassifier.set("javadoc")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                artifact(dokkaJavadocJar)
                groupId = project.group.toString()
                artifactId = providers.gradleProperty("POM_ARTIFACT_ID").get()
                version = project.version.toString()
                pom {
                    name.set(providers.gradleProperty("POM_NAME"))
                    description.set(providers.gradleProperty("POM_DESCRIPTION"))
                    inceptionYear.set(providers.gradleProperty("POM_INCEPTION_YEAR"))
                    url.set(providers.gradleProperty("POM_URL"))
                    licenses {
                        license {
                            name.set(providers.gradleProperty("POM_LICENCE_NAME"))
                            url.set(providers.gradleProperty("POM_LICENCE_URL"))
                            distribution.set(providers.gradleProperty("POM_LICENCE_DIST"))
                        }
                    }
                    developers {
                        developer {
                            id.set(providers.gradleProperty("POM_DEVELOPER_ID"))
                            name.set(providers.gradleProperty("POM_DEVELOPER_NAME"))
                            url.set(providers.gradleProperty("POM_DEVELOPER_URL"))
                        }
                    }
                    scm {
                        url.set(providers.gradleProperty("POM_SCM_URL"))
                        connection.set(providers.gradleProperty("POM_SCM_CONNECTION"))
                        developerConnection.set(providers.gradleProperty("POM_SCM_DEV_CONNECTION"))
                    }
                }
            }
        }
        repositories {
            maven {
                name = "centralBundle"
                url = layout.buildDirectory.dir("central-staging").get().asFile.toURI()
            }
        }

        val publishUrl = providers.gradleProperty("PUBLISH_URL")
        if (publishUrl.isPresent) {
            repositories {
                maven {
                    name = "remote"
                    url = uri(publishUrl.get())
                    credentials {
                        username = providers.gradleProperty("PUBLISH_USER").orNull
                            ?: System.getenv("PUBLISH_USER")
                        password = providers.gradleProperty("PUBLISH_PASSWORD").orNull
                            ?: System.getenv("PUBLISH_PASSWORD")
                            ?: System.getenv("GITHUB_TOKEN")
                    }
                }
            }
        }
    }

    signing {
        useGpgCmd()
        sign(publishing.publications["release"])
    }
}

val centralStagingDirectory = layout.buildDirectory.dir("central-staging")
val centralVersionDirectory = centralStagingDirectory.map {
    it.dir(
        "${project.group.toString().replace('.', '/')}/" +
            "${providers.gradleProperty("POM_ARTIFACT_ID").get()}/${project.version}",
    )
}

val cleanCentralStaging by tasks.registering(Delete::class) {
    delete(centralStagingDirectory)
}

tasks.matching { it.name == "publishReleasePublicationToCentralBundleRepository" }.configureEach {
    dependsOn(cleanCentralStaging)
}

val generateCentralBundleChecksums by tasks.registering {
    description = "Generates MD5 and SHA-1 checksums required by Maven Central"
    group = "publishing"
    dependsOn("publishReleasePublicationToCentralBundleRepository")

    doLast {
        val versionDirectory = centralVersionDirectory.get().asFile
        check(versionDirectory.isDirectory) {
            "Central staging directory was not generated: $versionDirectory"
        }

        versionDirectory.listFiles()
            ?.filter {
                it.isFile && it.extension !in setOf("md5", "sha1", "sha256", "sha512")
            }
            ?.forEach { artifact ->
                listOf("MD5" to "md5", "SHA-1" to "sha1").forEach { (algorithm, extension) ->
                    val digest = MessageDigest.getInstance(algorithm)
                        .digest(artifact.readBytes())
                        .joinToString("") { byte -> "%02x".format(byte) }
                    artifact.resolveSibling("${artifact.name}.$extension").writeText(digest)
                }
            }
    }
}

val centralBundle by tasks.registering(Zip::class) {
    description = "Creates a Maven Central Portal deployment bundle"
    group = "publishing"
    dependsOn(generateCentralBundleChecksums)

    from(centralStagingDirectory) {
        include(
            "${project.group.toString().replace('.', '/')}/" +
                "${providers.gradleProperty("POM_ARTIFACT_ID").get()}/${project.version}/**",
        )
    }
    destinationDirectory.set(layout.buildDirectory.dir("distributions"))
    archiveFileName.set(
        providers.gradleProperty("POM_ARTIFACT_ID").zip(
            providers.gradleProperty("VERSION_NAME"),
        ) { artifactId, version -> "$artifactId-$version-central-bundle.zip" },
    )
}
