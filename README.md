# java 21 template project

Uses the following:
* [Java 21 LTS](https://bell-sw.com/pages/downloads/#jdk-21-lts) 
* maven wrapper with `maven 3.6.3`

## Build & Run locally

* Build project using maven wrapper first
```bash
./mvnw clean package -DskipTests
```
* Execute fat JAR file using
```bash
./run.sh
```
Note: we can't execute jar file directly b/c some incubator and preview features should be enabled 
using `--enable-preview` JVM parameters.

## Unit tests.

For unit tests execute the following command
```bash
./mvnw clean test
```

## Code Style

Code formatted using [spotless-maven-plugin](https://github.com/diffplug/spotless/tree/master/plugin-maven ). The `spotless:apply` called just before the compilation 
phase to format code properly.

## Project Loom and co.

* [JEP 444: Virtual Threads](https://openjdk.org/jeps/444)
* [JEP 453: Structured Concurrency (Preview)](https://openjdk.org/jeps/453)
* [JEP 429: Scoped Values (Incubator)](https://openjdk.org/jeps/429)


