[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?logo=apache&style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0)
[![Version](https://img.shields.io/maven-central/v/dev.jayo/jayo-scheduler?logo=apache-maven&color=&style=flat-square)](https://search.maven.org/artifact/dev.jayo/jayo-scheduler)
[![Java](https://img.shields.io/badge/Java-17-ED8B00?logo=openjdk&logoColor=white&style=flat-square)](https://www.java.com/en/download/help/whatis_java.html)

# Jayo Scheduler

A simple scheduler for the JVM.

Jayo Scheduler's source code is derived and inspired from a small part of [OkHttp](https://github.com/square/okhttp),
but does not preserve backward compatibility with it.

It is available on Maven Central.

Gradle:
```groovy
dependencies {
    implementation("dev.jayo:jayo-scheduler:X.Y.Z")
}
```

Maven:
```xml

<dependency>
    <groupId>dev.jayo</groupId>
    <artifactId>jayo-scheduler</artifactId>
    <version>X.Y.Z</version>
</dependency>
```

The Jayo Scheduler code is written in Java without the use of any external dependencies, to be as light as possible.

Jayo Scheduler requires Java 17 or more recent.

*Contributions are very welcome, simply clone this repo and submit a PR when your fix, new feature, or optimization is
ready!*

## License

[Apache-2.0](https://opensource.org/license/apache-2-0)

Copyright (c) 2024-present, pull-vert and Jayo contributors
