FROM lucee/lucee:latest
LABEL maintainer="Azusa Pacific University"

# Drop the pssdk shadow jar (pssdk + psjoa + logback) into Tomcat's shared lib so
# the system classloader can see psft.pt8.joa.* and edu.apu.pssdk.* from CFML.
COPY build/libs/pssdk-example-lucee-all.jar /usr/local/tomcat/lib/

# Copy the CFML webroot into the default Lucee webapp.
COPY webroot/ /var/www/

# JVM tuning + PSJOA Jolt non-TLS flag are set in docker-compose.yml via JAVA_TOOL_OPTIONS.

EXPOSE 8888
