###############################################################################
# Dockerfile to support insecure instance running on localhost, port 80
###############################################################################

## Run this files from the top level directory using:
## 'docker build -f docker/Dockerfile.http .'

FROM maven:3.6.1-jdk-11-slim AS MAVEN_TOOL_CHAIN
WORKDIR /tmp/

COPY ./pom.xml /tmp/
COPY ./src /tmp/src/

RUN mvn package -Dmaven.test.skip=true


###############################################################################
# 1.0 Create a cutdown JDK image tailored to the needs of the application
###############################################################################

FROM alpine:3.7

ENV LANG en_US.UTF-8
ENV LANGUAGE en_US:en
ENV LC_ALL en_US.UTF-8
RUN ZULU_ARCH=zulu11.2.3-jdk11.0.1-linux_musl_x64.tar.gz && \
    INSTALL_DIR=/usr/lib/jvm && \
	BIN_DIR=/usr/bin && \
	MAN_DIR=/usr/share/man/man1 && \
	ZULU_DIR=$( basename ${ZULU_ARCH} .tar.gz ) && \
	wget -q https://cdn.azul.com/zulu/bin/${ZULU_ARCH} && \
	mkdir -p ${INSTALL_DIR} && \
	tar -xf ./${ZULU_ARCH} -C ${INSTALL_DIR} && rm -f ${ZULU_ARCH} && \
	cd ${BIN_DIR} && find ${INSTALL_DIR}/${ZULU_DIR}/bin -type f -perm -a=x -exec ln -s {} . \; && \
	mkdir -p ${MAN_DIR} && \
	cd ${MAN_DIR} && find ${INSTALL_DIR}/${ZULU_DIR}/man/man1 -type f -name "*.1" -exec ln -s {} . \; && \
	java -version


################################################################################
## 2.0 Define the rest of our application
################################################################################

# This script takes one argument - the name of the jar file containing
# the Spring Boot application.
ARG JAR_FILE

# This port must be open for TCP and UDP when the connection
# is via a channel access gateway.
EXPOSE 5062

# This port must be open for TCP and UDP when the connection
# is va normal IOC
EXPOSE 5064

# This port must be open for UDP to ensure that the EPICS client
# application sees the beacon messages sent to the local
# CA repeater.
EXPOSE 5065

# The keystore password must be supplied as an argument to the Docker
# run command. The keystore itself must be provided in the config
# directory via an external mount.
ENV KEYSTORE_PASS "XXXXXX"

# Document the ports that will be exposed by the Spring Boot Application
EXPOSE 443

# Setup the container so that it defaults to the timezone of PSI. This can
# always be overridden later. This step is important as the timezone is used
# in all log messages and is reported on the GUI.
ENV TZ="Europe/Zurich"

# Document the fact that this image will normally be run as root.
USER root

# Set the working directory
WORKDIR /root


###############################################################################
# 3.0 Install any additional applications
###############################################################################

RUN apk update && apk add --no-cache bash

###############################################################################
# 4.0 Set up the application project structure
###############################################################################

# Create the directories needed by this application
RUN mkdir log config lib

# Populate the application directories as appropriate
COPY --from=MAVEN_TOOL_CHAIN /tmp/target/wica-stream-1.0.0-RELEASE.jar lib/jarfile.jar
COPY ./src/main/resources/application.docker.http.properties config
COPY ./src/main/resources/logback_config.xml config


###############################################################################
# 5.0 Define the exposed volumes
###############################################################################

VOLUME /root/log
VOLUME /root/config
VOLUME /root/web
VOLUME /root/wica


###############################################################################
# 6.0 Define the ENTRYPOINT
###############################################################################

# Run the application on the Java 11 module path invoking the docker-run configuration profile
# and passing the contents of the SSH Deploy Key.
ENTRYPOINT exec java \
           -Dspring.config.location=config/application.docker.http.properties \
           -p lib/jarfile.jar \
           --add-modules ALL-DEFAULT \
           -m jarfile XXXX
