#!/bin/bash

export EPICS_CA_ADDR_LIST=localhost
export TARGET_PORT=80
export RELEASE=latest
export WEBDIR=/Users/simon/wica/web
export CONFIG=/Users/simon/wica/config

docker run --name wica-stream -t --rm \
  -p $TARGET_PORT:80 \
  -p 5062:5062/udp -p 5062:5062/tcp -p 5064:5064/udp -p 5064:5064/tcp -p 5065:5065/udp \
  -v $WEBDIR:/root/web \
  -v $CONFIG:/root/config \
  paulscherrerinstitute/wica-stream:$RELEASE


