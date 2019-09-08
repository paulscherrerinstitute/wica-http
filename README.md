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

## Running the server locally

   1. Get the release.
   
      The release names for this project follow the  [semantic versioning](https://semver.org/) naming convention
      proposed on the GitHub site.
      
      Examples: 1.0.0, 1.1.0, 1.2.3.rc1, 1.2.3.rc2, 7.1.5.rc19

      ```
      wget https://github.com/paulscherrerinstitute/wica-http/releases/download/<release>/wica-http-<release>.jar
      ```

   2. [Optional]: Set up the EPICS Channel-Access environment variables
    
      These should be set to to communicate with the process veriables on the backend IOC's that you 
      want to make accessible.
      
      See the section heading below for the supported variables and their default values.
      
      Examples:
      ```
         export EPICA_CA_ADDR_LIST=IOC1:5064
         export EPICA_CA_ADDR_LIST=<my_channel_access_gateway_server:5062
      ```
  
   3. Run the server.
      ```
         java -jar wica-http-<release>.jar 
      ```
      
   4. Check the server is running ok by navigating to the admin page.
      ```
         http://localhost:8080/admin
      ```
      
   5. Check the connection to the backend control system.
   
      Read your favourite EPICS channel (which must be accessible on your local network).
      ```
         http://localhost:8080/ca/channel/<pvName>
      ```   

   6. Run the test server
   
      The Wica-Http Server provides a very simple EPICS database at the following endpoint:
      ```
         http://localhost:8080/demo/simple.db
      ```   
      Download the DB file to your local filesystem and run it on your local network on your favourite 
      (Soft ?) IOC. The Wica-Http Server provides an equally unimpressive (! :-) ) web page to go with it at 
      the following location:
      ```
         http://localhost:8080/demo/simple.html
      ```
     
      When you navigate to this page the server should connect to the database and the counters should start 
      incrementing. 
  
   7. Start Developing
      
      If the previous step has worked successfully then you can save the page source of 'simple.html' to your 
      local filesystem and start to edit the html **wica-channel-name** attributes to reflect the process 
      variable names in your own local control system environment.
      
      If you reload the page from the browser then the server should initiate communication with your backend
      control channels.
      
      **Important Note:** 
      
      if you take this approach described above you will need to temporarily **DISABLE THE CORS SECURITY 
      CHECK** in your local browser. This is done in various ways depending on your browser (Chrome, 
      Safari, Firefox) and platform (Linux, OSX, Windows).
 
## Running inside a docker container

   Further details coming soon. :-)   


# EPICS Channel Access Environment Variables

The Wica-Http server is currently (2019-09-07) configured to work with PSI's native Java implementation 
of the [EPICS](https://epics-controls.org/) control system client side channel-access protocol. As such 
it respects the normal conventions with respect to environmental variables, including:

| Property                | Default Value | Desciption |
|-------------------------|---------------| ---------- |
|EPICS_CA_ADDR_LIST       |(empty)        | Address list to use when searching for channels.  |
|EPICS_CA_AUTO_ADDR_LIST  |true           | Automatically build up search address list.       |
|EPICS_CA_CONN_TMO        |30.0           | Disconnect timeout detection interval in seconds. |
|EPICS_CA_BEACON_PERIOD   |15.0           | Rate at which I'm alive beacons will be sent.     |
|EPICS_CA_REPEATER_PORT   |5065           | Channel Access Repeater Listening Port.           |
|EPICS_CA_SERVER_PORT     |5064           | Channel access server port.                       |
|EPICS_CA_MAX_ARRAY_BYTES |1000000        | Maximum size in bytes of an array/waveform.       |
   
For further information see the relevant section of the 
[EPICS Channel Access Reference Manual](https://epics.anl.gov/base/R3-14/12-docs/CAref.html) .  
   
   
# HTTP Endpoints 

The server supports the following endpoints.

### Download the Wica Javascript Client Library
```
GET /wica/wica.js
```

### Get the Value of a Channel

Returns JSON string representation of the value of the channel. Optional parameters can be specified for:

* the timeout in milliseconds.
* the numeric scale to be used when returning a channel value.
* the fields to be returned in the JSON representation. For an EPICS system these can be any of 'val', 'sevr', 'ts'.

```
GET /ca/channels/<channelName>[?timeout=XXX][&fieldsOfInterest=YYY;ZZZ][&numericScale=N]
```

Example:
```
GET http://localhost:8080/ca/channel/wica:test:counter01?timeout=50&fieldsOfInterest=val%3Bsevr&numericScale=4

HTTP/1.1 200 
Content-Type: application/json;charset=UTF-8
Content-Length: 30
Date: Sat, 07 Sep 2019 00:34:11 GMT

{
  "sevr": "0",
  "val": 188200.0000
}

Response code: 200; Time: 108ms; Content length: 30 bytes
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

* See the [CHANGELOG](CHANGELOG.md) file for further information.
* See also the project's [Issue Board](https://github.com/paulscherrerinstitute/wica-http/issues).

# Contact

If you have questions please contact: 'simon.rees@psi.ch'.