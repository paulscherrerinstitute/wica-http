###############################################################################
# 1.0 Create a cutdown JDK image tailored to the needs of the application
###############################################################################

# Start with the Standard OpenJDK release
FROM openjdk:9-jdk as build_jdk

WORKDIR /app

# Create a reduced JVM with just those modules that are required by the
# autodeployer application.

# Note: the modules in the list below were identified by using the JDK 'jdeps'
# tool to analyse the dependencies in the autodeployer über jar. Obviously the
# list will need to be adjusted when/if new features are added.

RUN jlink --module-path $JAVA_HOME/jmods \
          --add-modules java.base,java.desktop,java.instrument,java.logging,java.management,java.naming,java.prefs,java.rmi,java.security.jgss,java.scripting,java.sql,java.transaction,java.xml,java.xml.bind,java.xml.ws,java.xml.ws.annotation,jdk.httpserver \
          --output my_java \
          --compress 2 \
          --strip-debug \
          --no-header-files \
          --no-man-pages

###############################################################################
# 2.0 Get support for EPICS
###############################################################################

##
## Enable the following block when EPICS is needed in the container for eg
## debugging purposes.
##
## FROM debian:stable-slim as build_epics
##
## WORKDIR /epics
##
#
# RUN DEBIAN_FRONTEND=noninteractive apt-get update && \
#     apt-get install -y wget &&  \
#     apt-get install -y perl &&  \
#     apt-get install -y gcc &&  \
#     apt-get install -y make &&  \
#     apt-get install -y g++ &&  \
#     apt-get install -y libreadline-dev &&  \
#     rm -rf /var/lib/apt/lists/* /tmp/*
#
# RUN wget https://epics.anl.gov/download/base/baseR3.14.12.7.tar.gz && \
#     tar xvf baseR3.14.12.7.tar.gz
#
# RUN cd base-3.14.12.7 ; make
#

###############################################################################
# 3.0 Now create a debian image for deploying the application
###############################################################################

FROM debian:stable-slim

# This script takes one argument - the name of the jar file containing
# the Spring Boot application.
ARG JAR_FILE

# Copy over the cutdown Java runtime that was created in the first stage
# of the build above.
ENV JAVA_HOME=/opt/jdk \
    PATH=${PATH}:/opt/jdk/bin

COPY --from=build_jdk /app/my_java/ $JAVA_HOME


##
## Enable the following block when EPICS is needed in the container for eg
## debugging purposes.
##
#
#ENV EPICS_HOME=/epics
#COPY --from=build_epics /epics/base-3.14.12.7/ $EPICS_HOME
#
#RUN DEBIAN_FRONTEND=noninteractive apt-get update && \
#    apt-get install -y libreadline-dev


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
# 8443 is the production port.
EXPOSE 8080
EXPOSE 8443

# Setup the container so that it defaults to the timezone of PSI. This can
# always be overridden later. This step is important as the timezone is used
# in all log messages and is reported on the GUI.
ENV TZ="Europe/Zurich"

# Document the fact that this image will normally be run as root. The
# fact that the deployment script may need to manipulate file ownership
# and permissions forces this.
USER root

# Set the working directory
WORKDIR /root


###############################################################################
# 4.0 Install any additional applications
###############################################################################

# Add the dependencies of the deploy scripts, including python and git
# Added basic vim editor to ease debugging (can be removed later in production).
RUN DEBIAN_FRONTEND=noninteractive apt-get update && \
    apt-get install -y openssh-client &&             \
    apt-get clean &&                                 \
    rm -rf /var/lib/apt/lists/* /tmp/*

###############################################################################
# 5.0 Set up the application project structure
###############################################################################

# Create the directories needed by this application
RUN mkdir log config lib

# Populate the application directories as appropriate
COPY ./target/${JAR_FILE} lib/jarfile.jar

COPY ./src/main/resources/config/keystore.jks config
COPY ./src/main/resources/application-docker-run.properties config
COPY ./src/main/resources/docker_logback_config.xml config


###############################################################################
# 6.0 Define the exposed volumes
###############################################################################

VOLUME /root/.ssh
VOLUME /root/log
VOLUME /root/config


###############################################################################
# 7.0 Define the ENTRYPOINT
###############################################################################

# Run the application on the Java 9 module path invoking the docker-run configuration profile
# and passing the contents of the SSH Deploy Key
ENTRYPOINT java -Dspring.config.location=config/application-docker-run.properties \
           -p lib/jarfile.jar \
           --add-modules ALL-DEFAULT \
           -m jarfile \
           "$KEYSTORE_PASS"