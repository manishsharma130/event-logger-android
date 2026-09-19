plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
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

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
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
