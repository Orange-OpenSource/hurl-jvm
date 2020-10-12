# Hurl JVM

## Introduction

Hurl JVM is a [Hurl](https://hurl.dev) client for the JVM platform, written in Kotlin. 
Hurl JVM is available as a standalone jar, launchable in a shell (ex: `java -jar hurl.jar test.hurl`).
Hurl JVM try to have the same functionality as the official [Hurl client](https://github.com/Orange-OpenSource/hurl).

[![deploy status](https://travis-ci.org/Orange-OpenSource/hurl-jvm.svg?branch=master)](https://travis-ci.org/Orange-OpenSource/hurl-jvm/)


## CLI Usage

```
usage: java -jar hurl.jar file
    --color             Colorize output (not yet implemented)
    --file-root <arg>   Specify the root directory for file inclusions
 -h,--help              This help text
 -i,--include           Include HTTP headers in the output
 -k,--insecure          Allow connections to SSL sites without certs
 -L,--location          Follow redirect
    --no-color          Do not colorize output (not yet implemented)
    --to-entry <arg>    Execute Hurl file to ENTRY_NUMBER (starting at 1).
                        Ignore the remaining of the file. It is useful for
                        debugging a session.
 -V,--version           Show version number and quit
 -v,--verbose           Make the operation more talkative
    --variable <arg>    Define variable (example: --variable answer=42)
 -x,--proxy <arg>       [PROTOCOL://]HOST[:PORT] Use proxy on given port,
                        only http proxy is supported
```

## Build

Hurl JVM need JDK 8 to be build against, and use [Gradle](https://gradle.org).

To build the project:

```
./gradlew build
```

To test and produce a coverage report:

```
./gradlew test
./gradlew hurl-core:jacocoTestReport
```


## Source Code Structure

- hurl-core: core modules, can be used as a library for building app using Hurl.
- hurl-cli: standalone CLI application for Hurl
- hurl-fmt: standalone CLI application for formatting Hurl files 

## Importing Hurl JVM modules in a Gradle Project  

### Gradle Groovy DSL

```
implementation 'com.orange.ccmd:hurl-core:1.0.40'
```

```
implementation 'com.orange.ccmd:hurl:1.0.40'
```

```
implementation 'com.orange.ccmd:hurlfmt:1.0.40'
```

### Gradle Kotlin DSL

```
implementation("com.orange.ccmd:hurl-core:1.0.40")
```

```
implementation("com.orange.ccmd:hurl:1.0.40")
```

```
implementation("com.orange.ccmd:hurlfmt:1.0.40")
```

### Apache Maven

```xml
<dependency>
  <groupId>com.orange.ccmd</groupId>
  <artifactId>hurl-core</artifactId>
  <version>1.0.40</version>
</dependency>
```

```xml
<dependency>
  <groupId>com.orange.ccmd</groupId>
  <artifactId>hurl</artifactId>
  <version>1.0.40</version>
</dependency>
```

```xml
<dependency>
  <groupId>com.orange.ccmd</groupId>
  <artifactId>hurlfmt</artifactId>
  <version>1.0.40</version>
</dependency>
```
