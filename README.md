# peoplesoft-sdk

A Java library to consume Peoplesoft Component Interfaces via GraalVM's Node.js runtime

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
is provided; You can base your project on the image like this:

```
FROM quay.apu.edu/intdev/pssdk
MAINTAINER Azusa Pacific University

ARG NPM_TOKEN

RUN useradd -m -g root app \
 && chmod -R 750 /home/app

WORKDIR /home/app

COPY .npmrc package*.json src/ .

RUN npm ci --omit=dev

EXPOSE 3000

USER app

CMD ["node", "--jvm", "--vm.cp=/opt/peoplesoft-sdk-all.jar", "src/index.js"]
```
