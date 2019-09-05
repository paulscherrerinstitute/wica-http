[![Build Status](https://travis-ci.org/paulscherrerinstitute/wica-http.svg?branch=master)](https://travis-ci.org/paulscherrerinstitute/wica-http) 

:construction:
Note: this README is still **under construction** and may contain incorrect or misleading information.

# Overview

This is the Wica-HTTP Git repository, one component of PSI's WICA software suite. 
 
WICA stands for *Web Interface for Controls Applications*. The basic idea is to support the streaming of live data 
from a distributed control system to update a user's web pages in real-time.
 
Wica comprises two main components:

* [Wica-HTTP](https://github.com/paulscherrerinstitute/wica-http) - this is a backend HTTP server which 
  receives incoming requests from the web and which generates live data streams containing information 
  for the control system points of interest.

* [Wica-JS](https://github.com/paulscherrerinstitute/wica-js) - this is a frontend Javascript library 
  which scans a user's web pages for HTML5 tags defining points of interest in the control system. The 
  library then generates requests to the backend server to obtain the necessary data and to update the 
  user's web pages in real-time.

Currently WICA interoperates with the EPICS Control Systems using its well established Channel Access (CA) protocol. 

# Main Features

* Provides a gateway to the backend control system (currently EPICS).
* Provides an out-of-the-box web server, that serves the Wica-JS library and/or the users custom web pages.
* Streams control system data using HTML5 Server-Sent-Event technology.
* Supports basic control system Get and Set functionality.
* Provides channel filtering (eg noise or rate limiting) and configurable numeric precision.
* Implemented as Java 11, Spring Boot 2 application based on latest Spring reactive stack.
* Runs either standalone (via fat jar) or in docker container (available on Docker Hub).
* Supports EPICS Channel Access communication using PSI's pure Java CA client library.
* Monitors EPICS channels, or polls them at configurable rates.

# Requirements

The only requirement for running the Wica-HTTP server is Java 11 JRE.

# Getting Started

1. Get the Wica-HTTP fat Jar.
1. Adjust the configuration file to reflect your local conditions
1. Run the Server
```
   java -p lib/jarfile.jar --add-modules ALL-DEFAULT -m jarfile [<keystore_password>]
```

1. Check the server is running ok

   Use your browser to access the '/admin' endpoint.
   Example: navigate to the following URL
   
   http[s]://<wica_server_host>/admin
   
1. Check the connection to the backend control system is working 
  
   Read your favourite EPICS channel
   http[s]://wica.psi.ch/ca/channel/<pvName>
   
   
# HTTP Endpoints 

The server supports the following endpoints.

### Download the Wica Javascript Client Library
```
GET /wica/wica.js
```

### Get the Value of a Channel
```
GET /ca/channels/<channelName>[?timeout=XXX]

Returns JSON string representation of the value of the channel. For a channel whose underlying data source is EPICS the returned information looks like this:
{"type":"STRING","conn":true,"val":"15.101","sevr":0,"stat":0,"ts":"2019-03-06T09:37:22.103198","wsts":"2019-03-06T09:37:22.103211","wsts-alt":1551865042103,"dsts-alt":1551865042103}
```

### Set the Value of a Channel
```
PUT /ca/channels/<channelName>
Content-Type: text/plain the new value

somevalue
```

### Create Wica Stream

```
POST /ca/streams
Content-Type: application/json

{ "channels" : [ { "name": "abc:def", "props": { "chan_propA": "AAA", "chan_propB": "BBB" } } ], 
     "props" : { "stream_propX": "XXX", "stream_propY": "YYY" } }

Returns <streamId> a unique reference string that can be used when getting the stream (see below).
```

### Subscribe to Wica Stream
```
GET /ca/streams/<streamId>

Returns an event stream.
```

The returned event stream contains the following message types:

__Channel Metadata Information__ (sent on initial connection, then every time there is a change.)
```
id:0
event:ev-wica-channel-metadata
data:{"AMAKI1:IST:2":{"type":"REAL","egu":"A","prec":3,"hopr":72.000000,"lopr":-72.000000,"drvh":72.000000,"drvl":-72.000000,"hihi":NaN,"lolo":NaN,"high":NaN,"low":NaN}, ...etc }
:2019-03-06 09:39:39.407 - initial channel metadata
```

__Channel Initial Values__ (sent once, on initial cconnection)
```
id:0
event:ev-wica-channel-metadata
data:{"BMB1:STA:2":[{"val":"Faehrt","sevr":0},{"val":"Geschlossen","sevr":0},{"val":"Faehrt","sevr":0},{"val":"Offen","sevr":0},{"val":"Faehrt","sevr":0},{"val":"Geschlossen","sevr":0},{"val":"Faehrt","sevr":0},{"val":"Offen","sevr":0},{"val":"Faehrt","sevr":0},{"val":"Geschlossen","sevr":0},{"val":"Faehrt","sevr":0},{"val":"Offen","sevr":0},{"val":"Faehrt","sevr":0},{"val":"Geschlossen","sevr":0},{"val":"Faehrt","sevr":0},{"val":"Offen","sevr":0}]}
:2019-03-06 09:39:39.518 - initial channel values
```

__Channel Monitored Values__ (sent periodically eg every 100ms)
```
id:0
event:ev-wica-channel-value
data:{"MMAC3:STR:2":[{"val":15.069581,"sevr":0}],"SMA1Y:IST:2":[{"val":-0.966167,"sevr":0}],"QMA2:IST:2":[{"val":102.363586,"sevr":0}],"QMA1:IST:2":[{"val":-91.472626,"sevr":0}],"QMA3:IST:2":[{"val":-97.093582,"sevr":0}]}
:2019-03-06 09:39:54.526 - channel value changes
```

__Channel Polled Values__ (sent periodically eg every second)
```
id:0
event:ev-wica-channel-value
data:{"MMAC3:STR:2##2":[{"val":15.069581,"ts":"2019-03-06T09:39:54.527468"}],"CMJSEV:PWRF:2##2":[{"val":113.888885,"ts":"2019-03-06T09:39:54.527522"}],"EMJCYV:IST:2##2":[{"val":0.922709,"ts":"2019-03-06T09:39:54.527459"}]}
:2019-03-06 09:39:54.528 - polled channel values
```

__Channel Heartbeat__ (sent periodically eg every 15 seconds)
```
id:0
event:ev-wica-server-heartbeat
data:2019-03-06T09:39:54.348562
:2019-03-06 09:39:54.348 - server heartbeat
```

# Wica API Documentation

The Wica API documentation (Javadoc) is available [here](https://paulscherrerinstitute.github.io/wica-http/)


# Project Changes and Tagged Releases

* See the [CHANGELOG](../CHANGELOG.md) file for further information.
* See also the project's [Issue Board](https://github.com/paulscherrerinstitute/wica-http/issues).
