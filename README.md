# java 21 template project

Uses the following:
* java 21 early access (set through jenv, check `.java-version` file)
* maven wrapper with maven 3.6.3

## Build & Run locally

* Build project using maven wrapper first
```bash
./mvnw clean package -DskipTests
```
* Execute fat JAR file using
```bash
./run.sh
```

## Unit tests.

For unit tests execute the following command
```bash
./mvnw clean test
```

