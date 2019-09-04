#!/bin/bash

export TARGET_PORT=80
export RELEASE=latest
export WEBDIR=/Users/simon/wica/web
export CONFIG=/Users/simon/wica/config

#docker run -it --name wica-js-container -d paulscherrerinstitute/wica-js:latest

docker run --name wica-stream -t --rm \
  -p $TARGET_PORT:80 \
  -p 5062:5062/udp -p 5062:5062/tcp -p 5064:5064/udp -p 5064:5064/tcp -p 5065:5065/udp \
  -v $WEBDIR:/public/web \
  --volumes-from wica-js-container \
  -e EPICS_CA_ADDR_LIST=192.168.1.116 \
  paulscherrerinstitute/wica-stream:$RELEASE
