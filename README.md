[![Build Status](https://travis-ci.org/paulscherrerinstitute/wica-http.svg?branch=master)](https://travis-ci.org/paulscherrerinstitute/wica-http) 

:construction:
Note: this README is still **under construction** although hopefully, now, substantially correct. 

Please send any revision suggestions to 'simon.rees@psi.ch'.

# Overview

This is the **Wica-HTTP** git repository, one component of PSI's WICA software suite. 
 
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
* Provides an out-of-the-box web server, that serves the Wica-JS library and/or the user's custom web pages.
* Streams control system data using HTML5 Server-Sent-Event technology.
* Supports basic control system Get and Set functionality.
* Provides channel filtering (eg noise or rate limiting) and configurable numeric precision.
* Implemented as Java 11, Spring Boot 2 application based on latest Spring reactive stack.
* Runs either standalone (via fat jar) or in docker container (available on Docker Hub).
* Supports EPICS Channel Access communication using PSI's pure Java CA client library.
* Monitors EPICS channels, or polls them at configurable rates.

# Requirements

The only requirement for running the Wica-HTTP server is a **Java 11 JRE**.

# Getting Started

## Running the server locally

   1. Get the release.
   
      The release names for this project follow the  [semantic versioning](https://semver.org/) naming convention
      proposed on the GitHub site.
      
      Examples: 1.0.0, 1.1.0, 1.2.3.rc1, 1.2.3.rc2, 7.1.5.rc19

      ```
      wget https://github.com/paulscherrerinstitute/wica-http/releases/download/<rel>/wica-http-<rel>.jar
      ```

   1. [Optional]: Set up the EPICS Channel-Access environment variables.
    
      These should be setup to to communicate with the process veriables on the backend IOC's that you 
      want to make accessible. See the EPICS control control system support document 
      [here](EPICS.md#support-for-epics-channel-access-environment-variables) for a list of supported 
      variables and their default values.
      
      Examples:
      ```
      export EPICA_CA_ADDR_LIST=IOC1:5064
      export EPICA_CA_ADDR_LIST=<my_channel_access_gateway_server:5062
      ```
  
   1. Run the server.
      ```
      java -jar wica-http-<rel>.jar 
      ```
      
   1. Check the server is running ok by navigating to the admin page.
      ```
      http://localhost:8080/admin
      ```
      
   1. Check the connection to the backend control system.
   
      Read your favourite EPICS channel (which must be accessible on the local network).
      ```
      http://localhost:8080/ca/channel/<pvName>
      ```   

   1. Run and connect to the internal test server.
   
      The Wica-Http Server provides a very simple EPICS test database at the following endpoint:
      ```
      http://localhost:8080/test/test.db
      ```   
      Download the DB file to your local filesystem and run it on your local network on your favourite 
      (Soft ?) IOC. The Wica-Http Server provides an equally unsophisticated :wink: web page to go with 
      it here:
      ```
      http://localhost:8080/test/test.html
      ```
     
      When you navigate to this page the server should connect to the database and the counters should start 
      incrementing. 
  
   1. Start Developing.
      
      If the previous step worked successfully then the system is ready for something more interesting. 
      
      Save the page source of 'test.html' to your local filesystem and start to edit the html 
      **'data-wica-channel-name'** attributes to reflect PV's of interest in your own EPICS control 
      system environment.
      
      After reloading the page in the web browser (- don't forget, where necessary, to clear the cache -) 
      then the server should initiate communication with the backend control channels.
      
       **IMPORTANT NOTE** 
      
      If you take the approach described above you will need to temporarily **disable the CORS security check** 
      in your local browser. This is done in various ways depending on your browser type (Chrome, Safari, 
      Firefox), browser version and platform (Linux, OSX, Windows). Google is your friend here.

   1. Possible Next Steps
      
      It's time to get serious with your browser's developer tools. Google Chrome seems to work well in
      this capacity.
      
      * use the web browser's network analysis tool - to inspect the event stream coming from the server.
      
      * use the web browser's html element inspector to check the information that has been read from the 
        control system. The following attributes should now be present and updating in real-time.  
        * **'data-wica-stream-state'**
        * **'data-wica-channel-metadata'**
        * **'data-wica-channel-value-array'**
        * **'data-wica-channel-value-latest'**
        * **'data-wica-channel-connection-state'**
        * **'data-wica-channel-alarm-state'**
        
      * now the hard part starts: :wink:  writing web pages that leverage off the capabilities offered in this 
        web environment. Good Luck ! :smile:
              
## Running the server inside a docker container

   1. Get the release into your local docker environment.
   
      ```
      docker pull paulscherrerinstitute/wica-http:<rel>.jar
      ```

      Example:
      ```
      docker pull paulscherrerinstitute/wica-http:1.0.1
      ```

   1. [Optional]: Set up the EPICS Channel-Access environment variables.
    
      These should be setup to to communicate with the process veriables on the backend IOC's that you 
      want to make accessible. See the EPICS control control system support document 
      [here](EPICS.md#support-for-epics-channel-access-environment-variables) for a list of supported 
      variables and their default values.
      
      Examples:
      ```
      export EPICA_CA_ADDR_LIST=IOC1:5064
      export EPICA_CA_ADDR_LIST=<my_channel_access_gateway_server:5062
      ```
  
   1. Run the server.
      ```
      docker run -p 80:80 -p 5062:5062/udp -p 5062:5062/tcp -p 5064:5064/udp -p 5064:5064/tcp -p 5065:5065/udp \
       -e EPICS_CA_ADDR_LIST=${EPICA_CA_ADDR_LIST} paulscherrerinstitute/wica-http:<rel>.jar
      ```
      

# High Level Terms and Concepts

Whilst currently (2019-09-15) the Wica-Http server supports only a single backend control system (EPICS) and 
network protocol (Channel Access) its implementation is intended to be flexible enough to interoperate in the
future with other control systems and/or protocols. 

To achieve this the server strives to create an API which offers programming abstractions that are flexible 
enough to adapt to future needs, or at least which will not have to change too radically. 

The main abstractions are as follows:

## Wica Channel

A [WicaChannel](https://paulscherrerinstitute.github.io/wica-http/latest/ch/psi/wica/model/channel/WicaChannel.html) 
represents a readable or writable *control point* in the environment of the backend control system. 

A wica channel has:
* [WicaChannelName](https://paulscherrerinstitute.github.io/wica-http/latest/ch/psi/wica/model/channel/WicaChannelName.html) - 
  an abstraction which specifies the network protocol required to communicate with the control point, the name by 
  which it is known to the control system, together with an instance specifier (required to ensure uniqueness).
* [WicaChannelProperties](https://paulscherrerinstitute.github.io/wica-http/latest/ch/psi/wica/model/channel/WicaChannelProperties.html) - 
  an abstraction specifying the configuration of the channel, for example whether it *polls* or *monitors* the 
  underlying control system, whether or how the data obtained from the control system should be *filtered* and 
  how it should be  *serialized* when returned to the end-user.

## Wica Channel Metadata

The [WicaChannelMetadata](https://paulscherrerinstitute.github.io/wica-http/latest/ch/psi/wica/model/channel/WicaChannelMetadata.html) 
abstraction models the properties of the control point that are read out when it comes online but which thereafter 
remain unchanged. These include, typically, properties which describe the control point's underlying nature (for 
example the physical quantity that the control point represents, the expected operating limits, the values which 
correspond to error or warning condition etc. etc.)
  
## Wica Channel Value

The [WicaChannelValue](https://paulscherrerinstitute.github.io/wica-http/latest/ch/psi/wica/model/channel/WicaChannelValue.html)
abstraction provides properties to model the control point's instantaneous state. These include, typically, whether 
the channel is *online* or *offline*, the *timestamp* and *raw value* that was obtained when the channel was last 
read out, and whether an *alarm* or *warning* condition exists.
  
## Wica Stream

A [WicaStream](https://paulscherrerinstitute.github.io/wica-http/latest/ch/psi/wica/model/stream/WicaStream.html) 
represents a collection of wica channels that are grouped together for the purpose of real-time monitoring. 

A wica stream is created by an HTTP POST operation on the server. Thereafter, it can be subscribed to by an 
HTTP GET operation which returns a stream of Server-Sent-Event (SSE) messages reflecting the evolving state of 
the channels.

In addition to the set of wica channels which it owns a wica stream has:
* [WicaStreamId](https://paulscherrerinstitute.github.io/wica-http/latest/ch/psi/wica/model/stream/WicaStreamId.html) - 
this is a unique handle allocated by the server when a new stream is created.
* [WicaStreamProperties](https://paulscherrerinstitute.github.io/wica-http/latest/ch/psi/wica/model/stream/WicaStreamProperties.html) 
an abstraction specifying the configurable properties of the stream, including things which affect the stream's 
behaviour, and things which determine the default property values that will be assigned to each of the stream's 
underlying wica channels.

# Control System Support

Wica currently supports only **EPICS** by means of its **channel-access** protocol. 

See [this document](EPICS.md) for further information.


# Server Endpoints 

The server supports the following endpoints.

### Download the Wica Javascript Client Library
```
GET /wica/wica.js
```

### Download the EPICS Test Database
```
GET /test/test.db
```

### Get the EPICS Test Database HTML Page
```
GET /test/test.html
```

### Get the Value of a Channel

Returns a JSON string representation of the value of the channel. Optional parameters can be specified for:

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
Content-Type: text/plain

somevalue
```
Example Request:
```
PUT http://localhost:8080/ca/channel/wica:test:counter01
Content-Type: text/plain

999999
```
Example Response:
```
PUT http://localhost:8080/ca/channel/wica:test:counter01

HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 2
Date: Sun, 08 Sep 2019 14:45:53 GMT

OK

Response code: 200; Time: 103ms; Content length: 2 bytes
```

### Create a Wica Stream

Creates a new stream, allocating a new stream id, and initiating communication with the backend 
control system(s) to obtain the latest data for the control points for the wica stream's channels.

The wica stream publishes a continuous stream of Server Sent Event (SSE) messages of the following types: 

|SSE Event Message           |SSE Data Payload                                                     |SSE Comment                               | Periodicity                                                               | Description                                                       |
|----------------------------|-------------------------------------------------------------------- |------------------------------------------| ------------------------------------------------------------------------- | ------------------------------------------------------------------|
| 'ev-wica-server-heartbeat' | JSON String containing server timestamp.                            | timestamp + '- server heartbeat'         | Configurable.                                                             | Used to inform the client that the channel is still alive.        |
| 'ev-wica-channel-metadata' | JSON Object containing channel names and metadata.                  | timestamp + '- channel metadata'         | Configurable, but only sent for channels when they first come online.     | Delivers the new metadata for each channel in the stream.         | 
| 'ev-wica-channel-value'    | JSON Object containing channel names and array of monitored values. | timestamp + '- channel monitored values' | Configurable, sent for channels which have received new monitored values. | Delivers the new values for each monitored channel in the stream. |   
| 'ev-wica-channel-value'    | JSON Object containing channel names and array of polled values.    | timestamp + '- channel polled values'    | Configurable, sent for channels which have received new polled values.    | Delivers the new values for each polled channel in the stream.    |   

Note: currently (2019-09-17) the metadata message type is suppressed if none of the wica-channels have any new metadata.

The following configuration properties are supported on each wica-channel.

|Property     |Description                                                                                                      |
|-------------|---------------------------------------------------------------------------------------------------------------- |
| "daqmode"   |The data acquisition mode. Possible values: 'poll', 'monitor', 'poll-monitor', poll-and-monitor'.                |
| "pollint"   |The polling interval in milliseconds for a channel whose data acquisition mode implies polling.                  |
| "fields"    |Semicolon separated list specifying the fields which will be included in the 'ev-wica-channel-value' messages.   |
| "filter"    |The channel filter type.  Possible values: 'all-value', 'rate-limiter', 'last-n', 'one-in-m', 'change-filterer'. |
| "n"         |The value of the 'N' parameter for a last-n filter.                                                              |
| "m"         |The value of the 'M' parameter for a one-in-m filter.                                                            |
| "deadband"  |The value of the 'deadband' parameter for a change-filterer filter.                                              |
| "interval"  |The value of the 'interval' parameter (in milliseconds) for a rate-limiter filter.                               |
| "fields"    |Semicolon separated list specifying the fields which will be included in the 'ev-wica-channel-value' messages.   |


The following configuration properties are supported on a wica stream:

|Property     |Description                                                                                                    |
|-------------|-------------------------------------------------------------------------------------------------------------- |
| "hbflux"    |Defines the interval in milliseconds between successive SSE 'ev-wica-server-heartbeat' messages.               |
| "metaflux"  |Defines the interval in milliseconds between successive SSE 'ev-wica-channel-metadata' messages.               |
| "monflux"   |Defines the interval in milliseconds between successive SSE 'ev-wica-channel-value' monitor value messages.    |
| "pollflux"  |Defines the interval in milliseconds between successive SSE 'ev-wica-channel-value' polled value messages.     |

Note: additionally, **any of the channel properties may also be defined on a stream** to set the default value for the 
channels on that stream.

```
POST /ca/streams
Content-Type: application/json

{ "channels" : [ { "name": "abc:def", "props": { "chan_propA": "AAA", "chan_propB": "BBB" } } ], 
     "props" : { "stream_propX": "XXX", "stream_propY": "YYY" } }

Returns <streamId> a unique reference string that can be used when getting the stream (see below).
```

Example Request:
```
POST http://localhost:8080/ca/streams
Content-Type: application/json

{ "channels" : [ { "name": "wica:test:counter01", "props": { "daqmode": "monitor"  } },
                 { "name": "wica:test:counter02", "props": { "daqmode": "poll", "pollint": "5000" } }],
  "props" : { "prec": 2, "hbflux": "8000", "monflux": 1000, "pollflux": 2000 }
}
```

Example Response:
```
POST http://localhost:8080/ca/streams

HTTP/1.1 200 
Content-Type: text/plain;charset=UTF-8
Content-Length: 1
Date: Sun, 08 Sep 2019 15:29:21 GMT

3

Response code: 200; Time: 51ms; Content length: 1 bytes
```

### Subscribe to a Wica Stream
```
GET /ca/streams/<streamId>

Returns an event stream.
```

Example Request:
```
GET http://localhost:8080/ca/streams/3
```

The returned event stream contains the following message types:

__Channel Metadata Information__ (sent on initial connection, then every time there is a change.)

Example:
```
id:3
event:ev-wica-channel-metadata
data:{"wica:test:counter01":{"type":"REAL","egu":"","prec":0,"hopr":0.00,"lopr":0.00,"drvh":0.00,"drvl":0.00,"hihi":NaN,"lolo":NaN,"high":NaN,"low":NaN}}
:2019-09-08 17:30:28.181 - channel metadata
```

__Channel Monitored Values__ (sent periodically at user configured rate)
```
id:3
event:ev-wica-channel-value
data:{"wica:test:counter01":[{"sevr":"0","val":11042.00}]}
:2019-09-08 17:30:29.085 - channel monitored values
```

__Channel Polled Values__ (sent periodically at user configured rate)
```
id:3
event:ev-wica-channel-value
data:{"wica:test:counter02":[{"sevr":"0","val":61077.00}]}
:2019-09-08 17:30:34.077 - channel polled values
```

__Server Heartbeat__ (sent periodically eg every 15 seconds)
```
id:3
event:ev-wica-server-heartbeat
data:2019-09-08T17:30:36.078750
:2019-09-08 17:30:36.078 - server heartbeat
```

# Wica-HTTP API Documentation

The API documentation for the **latest development** release is always available [here](https://paulscherrerinstitute.github.io/wica-http/latest).

The API documentation for the **latest stable** release (**1.0.1**) is available [here](https://paulscherrerinstitute.github.io/wica-http/1.0.1).

The API documentation for **previous releases** can be found as follows:
```
https://paulscherrerinstitute.github.io/wica-http/<rel>
```

# Project Changes and Tagged Releases

* See the [CHANGELOG](CHANGELOG.md) file for further information.
* See also the project's [Issue Board](https://github.com/paulscherrerinstitute/wica-http/issues).

# Contact

If you have questions please contact: 'simon.rees@psi.ch'.
