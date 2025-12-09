# pssdk

Interact with Peoplesoft Component Interfaces using JavaScript and Python!

Without the Integration Broker!

You need to provide the psjoa.jar file yourself.

### Building

Run Gradle with the `shadowJar` task to have a single jar build.

```
$ ./gradlew clean build shadowJar
```

Build with docker:

```
$ docker build . -t quay.apu.edu/intdev/pssdk
```

And finally push the image to Quay:
```
$ docker push quay.apu.edu/intdev/pssdk
```

### Usage

You can use this project through the GraalJS Node.js project. A container image
is not provided; You need to provide your own `psjoa.jar` file and optionally a
logger (project support SLF4J) and package everything in a single JAR.

If you use Gradle, an example `build.gradle` would be:

```
plugins {
    id 'java-library'
    id("com.gradleup.shadow") version "9.1.0"
}

archivesBaseName = 'pssdk'

repositories {
    mavenCentral()
    maven {
        name "<your-repo-name>"
        url "https://<your-repo-url>"
        credentials {
            username "<your-user>"
            password "$System.env.YOUR_TOKEN"
        }
    }
}

dependencies {
    implementation group: 'edu.apu.pssdk', name: 'pssdk', version: '1.0.0-SNAPSHOT'
    implementation group: 'com.oracle.peoplesoft', name: 'psjoa', version: "$System.env.PSJOA_JAR_VERSION"
    implementation group: 'ch.qos.logback', name: 'logback-classic', version: '1.5.6'
}
```

Then, you can package your application in a Docker image like this:

```
FROM quay.apu.edu/intdev/graalnodejs
MAINTAINER Azusa Pacific University

RUN useradd -m -g root app \
 && chmod -R 750 /home/app

COPY build/libs/pssdk-all.jar /opt/pssdk/

WORKDIR /home/app

COPY package*.json ./

RUN npm ci --omit=dev

COPY src src

EXPOSE 3000

USER app

ENTRYPOINT ["node", "--jvm", "--vm.cp=/opt/pssdk/pssdk-all.jar", "src/index.js"]
```

### Environment Variables

The project can make use of the following environment variables:

```
PS_APPSERVER_HOSTPORT
PS_APPSERVER_DOMAINPW
PS_APPSERVER_USERNAME
PS_APPSERVER_PASSWORD
```

If those environment variables are properly set, a static call to
`AppServer.fromEnv()` would return an instance of `Appserver`.

Otherwise, you can create an instance of `AppServer` manually by providing
the required parameters as a `Map<String, String>` or a config object from
JavaScript.

> [!NOTE] Legal Disclaimer:
>
> GraalVM™, Java™, Oracle™, PeopleSoft™ are registered trademarks of
> Oracle Corporation and/or its affiliates. Other names may be trademarks of
> their respective owners. This project is not associated with, endorsed by,
> or sponsored by Oracle Corporation.
>
> - GraalVM: GraalVM is a high-performance runtime that provides significant
> improvements in application performance and efficiency. GraalVM is the
> intellectual property of Oracle Corporation.
> 
> - Java: Java is a registered trademark of Oracle Corporation in the United States
> and other countries.
> 
> - Oracle: Oracle is a registered trademark and is often associated with various
> software products and services.
> 
> - PeopleSoft: PeopleSoft is a registered trademark of Oracle Corporation.
