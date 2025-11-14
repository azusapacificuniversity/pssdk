FROM quay.apu.edu/intdev/graalpy
MAINTAINER Azusa Pacific University

WORKDIR /home/app

COPY requirements.txt .

RUN graalpy -m ensurepip --default-pip && \
    graalpy -m pip install -r requirements.txt

COPY build/libs/pssdk-example-flask-all.jar /opt/pssdk/

COPY src src

EXPOSE 5000

ENTRYPOINT [ \
  "graalpy", \
  "--jvm", \
  "--vm.DTM_ALLOW_NOTLS=Y", \
  "--vm.Dlogback.statusListenerClass=ch.qos.logback.core.status.OnConsoleStatusListener", \
  "--vm.cp=/opt/pssdk/pssdk-example-flask-all.jar", \
  "src/main.py" \
]
