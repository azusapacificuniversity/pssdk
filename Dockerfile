FROM quay.apu.edu/intdev/graalnodejs

ENV SSL_KEY_STORE_PATH=$JAVA_HOME/lib/security/cacerts

COPY ./build/libs/pssdk-all.jar /opt/pssdk/lib/
