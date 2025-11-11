FROM container-registry.oracle.com/graalvm/nodejs-community:latest
MAINTAINER Azusa Pacific University

ARG NPM_TOKEN

RUN useradd -m -g root app \
 && chmod -R 750 /home/app

WORKDIR /home/app

COPY package*.json .

RUN npm i

COPY build/libs/pssdk-express-all.jar /opt/pssdk/

COPY src src

EXPOSE 3000

USER app

ENTRYPOINT [ \
  "node", \
  "--jvm", \
  "--vm.Dlogback.statusListenerClass=ch.qos.logback.core.status.OnConsoleStatusListener", \
  "--vm.DTM_ALLOW_NOTLS=Y", \
  "--vm.cp=/opt/pssdk/pssdk-express-all.jar", \
  "src/index.js", \
  "--no-opt" \
]
