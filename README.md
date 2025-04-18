# peoplesoft-sdk

A Java library to consume Peoplesoft Component Interfaces via GraalVM's Node.js runtime

### Building

Run Gradle with the `shadowJar` task to have a single jar build.

```
$ ./gradlew clean build shadowJar
```

### Usage

You can use this project through the GraalJS Node.js project. A container image
is provided; You can base your project on the image like this:

```
FROM quay.apu.edu/intdev/pssdk
MAINTAINER Azusa Pacific University

ARG NPM_TOKEN

COPY .npmrc .npmrc

COPY package*.json ./

RUN npm ci --omit=dev

COPY src src

EXPOSE 3000

CMD ["node", "--jvm", "--vm.cp=peoplesoft-sdk-all.jar", "src/index.js"]
```
