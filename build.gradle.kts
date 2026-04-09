import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.tooling.GradleConnector
import org.jetbrains.kotlin.gradle.dsl.ExplicitApiMode.Strict
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_3
import kotlin.jvm.optionals.getOrNull
import org.jetbrains.kotlin.config.KotlinCompilerVersion.VERSION as KOTLIN_VERSION

println("Using Gradle version: ${gradle.gradleVersion}")
println("Using Kotlin compiler version: $KOTLIN_VERSION")
println("Using Java compiler version: ${JavaVersion.current()}")

plugins {
    alias(libs.plugins.kotlin)
    `java-library`
    `maven-publish`
    signing
    alias(libs.plugins.release)
    `java-test-fixtures`
    alias(libs.plugins.kover)
}

val versionCatalog: VersionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
fun catalogVersion(lib: String) =
    versionCatalog.findVersion(lib).getOrNull()?.requiredVersion
        ?: throw GradleException("Version '$lib' is not specified in the toml version catalog")

val javaVersion = catalogVersion("java").toInt()

kotlin {
    compilerOptions {
        languageVersion.set(KOTLIN_2_3)
        apiVersion.set(KOTLIN_2_3)
        explicitApi = Strict
        freeCompilerArgs.addAll(
            "-Xnullability-annotations=@org.jspecify.annotations:strict", // not really sure if this helps ;)
            "-opt-in=kotlin.contracts.ExperimentalContracts",
        )
    }
}

repositories {
    mavenCentral()
}

dependencies {
    api("org.jspecify:jspecify:${catalogVersion("jspecify")}")

    testImplementation("org.junit.jupiter:junit-jupiter:${catalogVersion("junit")}")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher:${catalogVersion("junit")}")
    testRuntimeOnly("org.slf4j:slf4j-jdk-platform-logging:${catalogVersion("slf4j")}")
    testRuntimeOnly("ch.qos.logback:logback-classic:${catalogVersion("logback")}")

    testFixturesApi(testFixtures("dev.jayo:jayo:${catalogVersion("jayo")}"))
    testFixturesApi("org.jetbrains.kotlin:kotlin-stdlib")
    testFixturesApi("org.assertj:assertj-core:${catalogVersion("assertj")}")
}

kover {
    reports {
        verify {
            rule {
                bound {
                    minValue = 85
                }
            }
        }
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(javaVersion)
    }

    withJavadocJar()
}

val testJavaVersion = System.getProperty("test.java.version", "").toIntOrNull()
tasks.test {
    if (testJavaVersion != null) {
        javaLauncher = javaToolchains.launcherFor {
            languageVersion = JavaLanguageVersion.of(testJavaVersion)
        }
    }
    useJUnitPlatform()
    testLogging {
        events = setOf(TestLogEvent.PASSED, TestLogEvent.FAILED, TestLogEvent.SKIPPED)
        exceptionFormat = TestExceptionFormat.FULL
        showStandardStreams = true
    }
}

// Generate a source jar for main and testFixtures in jvm artifacts.
val sourcesJar by tasks.registering(Jar::class) {
    description = "A Source JAR containing sources for main and testFixtures"
    from(sourceSets.main.get().allSource, sourceSets.testFixtures.get().allSource)
    archiveClassifier.set("sources")
}

publishing {
    repositories {
        maven {
            url = uri(layout.buildDirectory.dir("repos/releases"))
        }
    }


    publications.withType<MavenPublication> {
        from(components["java"])

        artifact(sourcesJar)

        pom {
            name.set(project.name)
            description.set("Jayo Scheduler is a Java port of the Result<T> type from the Kotlin stdlib")
            url.set("https://github.com/jayo-projects/jayo-scheduler")

            licenses {
                license {
                    name.set("Apache-2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                }
            }

            developers {
                developer {
                    name.set("pull-vert")
                    url.set("https://github.com/pull-vert")
                }
            }

            scm {
                connection.set("scm:git@github.com/jayo-projects/jayo-scheduler")
                developerConnection.set("scm:git@github.com/jayo-projects/jayo-scheduler.git")
                url.set("https://github.com/jayo-projects/jayo-scheduler.git")
            }
        }
    }
}

signing {
    // Require signing.keyId, signing.password and signing.secretKeyRingFile
    sign(publishing.publications)
}

// workaround : https://github.com/researchgate/gradle-release/issues/304#issuecomment-1083692649
configure(listOf(tasks.release, tasks.runBuildTasks)) {
    configure {
        actions.clear()
        doLast {
            GradleConnector
                .newConnector()
                .forProjectDirectory(layout.projectDirectory.asFile)
                .connect()
                .use { projectConnection ->
                    val buildLauncher = projectConnection
                        .newBuild()
                        .forTasks(*tasks.toTypedArray())
                        .setStandardInput(System.`in`)
                        .setStandardOutput(System.out)
                        .setStandardError(System.err)
                    gradle.startParameter.excludedTaskNames.forEach {
                        buildLauncher.addArguments("-x", it)
                    }
                    buildLauncher.run()
                }
        }
    }
}

// when the Gradle version changes:
// -> execute ./gradlew wrapper, then remove .gradle directory, then execute ./gradlew wrapper again
tasks.wrapper {
    gradleVersion = "9.4.1"
    distributionType = Wrapper.DistributionType.ALL
}

