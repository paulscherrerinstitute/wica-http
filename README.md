# Overview

WICA stands for "Web Interface for Controls Applications". The idea is to provide web-based access to PSI's controls 
infrastructure. 

Currently this infrastructure is based on EPICS IOC's accessed by channel access protocol. Wica supports
this as its primary use case but its central abstractions are designed to be extensible to other additional 
control system types and/or network protocols as well.

The **Wica2** project is the successor to PSI's earlier [Wica](https://git.psi.ch/controls_highlevel_applications/ch.psi.wica)
project whose stated goal was to provide a: 

> _**"Simple, but powerful, Channel Access to REST service."**_ 

Wica2 includes the goal of the earlier project but expands the vision to target the following use case:

> _**"Create some software that will enable end-users to easily create their own customised web pages to
show the evolving, live status of one or more EPICS channels of interest."**_

Wica2 provides a REST backend service with similar functionality to Wica. Additionally it provides a 
frontend library which streams live data from the backend, and then subsequently uses the received 
information to dynamically update the end-user's web page.

Wica2 is based on technologies that are currently in active use within PSI's GFA Controls Section. The main 
technology differences between Wica and Wica2 are as follows:

| __Original WICA Project__                     | __WICA2 Project__                                       |
| :-------------------------------------------- | :------------------------------------------------------ |
| Backend runs on Glassfish Application Server. | Backend uses JavaSpring Boot containers (Tomcat/Netty). |
| Backend runs directly on linux host.          | Backend runs in Docker container.                       |
| Backend uses EPICS JCA/CAJ CA library.        | Backend uses PSI's EPICS CA client library.             |
| No direct support for frontend (webpages).    | Frontend uses JS library to leverage off HTML5 features (eg Server Sent Events (SSE), data-* attributes...)


# Simple Wica Webpage Example

The simplest Wica2 webpage looks like this:
```
<!DOCTYPE html>
<html lang="en">
<head>
   <meta charset="UTF-8"/>
   <title>My Awesome Epics Channel Viewer</title>
   <script src="/wica/wica.js" type="module"></script>
</head>

<body>
   <div data-wica-channel-name="abc:def"></div>
</body>

</html>
```
In this example a single channel named "abc:def" is being monitored. When the page is loaded the div element's
text content will be dynamically updated with the latest values received from the wica channel.

# How it Works

The principle of operation is as follows. The Wica JS library module is loaded after the rest of the webpage. The 
library scans the document from which it was loaded for elements whose 'data-wica-channel-name' attribute is set.
This attribute is used as a means of indicating that the element is "wica-aware". 

The library then communicates the channel names associated with all wica-aware elements to the Wica REST Server 
(on the backend) which starts monitoring the associated data sources and streaming back the channel metadata 
(eg alarm and display limits) and value information to the frontend.

In response to the received data stream the Wica JS library module then updates the following attributes of each 
wica-aware html element:

| __Attribute__                       | __Meaning__                                                | __Possible Values__                                       |
| :---------------------------------- | :----------------------------------------------------------| :-------------------------------------------------------- |
| data-wica-stream-state              | Contains status of connection to Wica Server.              | "connecting", "opened-XXX", "closed-XXX"                  |
| data-wica-channel-connection-state  | Contains status of connection to data source.              | "disconnected", "connected"                               |
| data-wica-channel-alarm-state       | Contains alarm status of data source.                      | "NO_ALARM", "MINOR_ALARM", "MAJOR_ALARM", "INVALID_ALARM" |
| data-wica-channel-metadata          | Contains last received metadata from data source.          | Format depends on data source.                            |
| data-wica-channel-value-latest      | Contains last received value from data source.             | Format depends on data source.                            |
| data-wica-channel-value-array       | Contains array of latest received values from data source. | Format depends on data source.                            |
 
Periodically (default = 100ms) the  Wica JS library module iterates through all wica-aware elements and performs the 
following actions:
1. if an element's 'onchange' attribute is defined or a custom 'onwica' event handler is defined - the library module
calls the handlers, providing them with the latest received channel metadata and value information.
1. if an element is of a type which supports a textControl attribute then this attribute is set to the latest channel value.

ToDo: document how the data-wica-rendering-hints attribute can be used to further control the behaviour.
ToDo: document how tooltips are supported.
ToDo document how CSS is used to perform colorisation of the element in the case of an alarm situation.
  
# Setting Wica Channel properties

In addition to the 'data-wica-channel-name' attribute one can supply a 'data-wica-channel-props' object to modify 
the properties of the channel. For example:
```
<!DOCTYPE html>
<html lang="en">
<head>
   <meta charset="UTF-8"/>
   <title>My Awesome Epics Channel Viewer</title>
   <script src="/wica/wica.js" type="module"></script>
</head>

<body>
   <div data-wica-channel-name="some_channel_of_interest" data-wica-channel-props='{ "prec" : 0 }'></div>
</body>

</html>
```

In the above example the precision of the channel is set to 0 decimal places. This means that when 
streamed down the wire the channel's numeric value is represented stream with zero decimal places.

The list of currently supported properties is as follows:

| __Property__       | __Description__                                                            |
| :----------------- | :------------------------------------------------------------------------- |
| daqMode            | Sets the Data Acquisition Mode. Can be monitoring or polling.              |
| pollratio          | Sets the number of polling cycles before a sample is taken.                |
| fields             | A semicolon delimited list defining the data fields that should be included when sending value information for this channel.|
| prec               | Sets the number of digits after the decimal point to be used when sending numeric information. |
| filter             | Sets the type of filtering to be used for this channel.                    |
| n                  | Sets the the number of samples (latest value sampling filter only)         |
| m                  | Sets the the cycle length (fixed cycle sampling filter only)               |
| interval           | Sets the the filter sampling interval (rate limiting sampling filter only) |
| deadband           | Sets the the filter deadband ( change filtering sampler only)              |

For the latest information please consult the [JS](http://controls_highlevel_applications.gitpages.psi.ch/ch.psi.wica2/js/wica) 
and [Javadoc](http://controls_highlevel_applications.gitpages.psi.ch/ch.psi.wica2/java) information which is built automatically 
with every software release.

# Wica REST Service API 

On the Wica Backend Server the following endpoints are provided for leveraging the functionality of the system.


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

__Channel Metadata Information__ (sent once)
```
id:0
event:ev-wica-channel-metadata
data:{"AMAKI1:IST:2":{"type":"REAL","egu":"A","prec":3,"hopr":72.000000,"lopr":-72.000000,"drvh":72.000000,"drvl":-72.000000,"hihi":NaN,"lolo":NaN,"high":NaN,"low":NaN}, ...etc }
:2019-03-06 09:39:39.407 - initial channel metadata
```

__Channel Initial Values__ (sent once)
```
id:0
event:ev-wica-channel-metadata
data:{"BMB1:STA:2":[{"val":"Faehrt","sevr":0},{"val":"Geschlossen","sevr":0},{"val":"Faehrt","sevr":0},{"val":"Offen","sevr":0},{"val":"Faehrt","sevr":0},{"val":"Geschlossen","sevr":0},{"val":"Faehrt","sevr":0},{"val":"Offen","sevr":0},{"val":"Faehrt","sevr":0},{"val":"Geschlossen","sevr":0},{"val":"Faehrt","sevr":0},{"val":"Offen","sevr":0},{"val":"Faehrt","sevr":0},{"val":"Geschlossen","sevr":0},{"val":"Faehrt","sevr":0},{"val":"Offen","sevr":0}]}
:2019-03-06 09:39:39.518 - initial channel values
```

__Channel Value Changes__ (sent periodically eg every 100ms)
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

### Download the Wica Javascript Client Library
```
GET /wica/wica.js
```

# Wica Javascript Client Library API

The Javascript client library contains a module named [client-api](http://controls_highlevel_applications.gitpages.psi.ch/ch.psi.wica2/js/wica/module-client-api.html)

This is the start point for leveraging Wica functionality on the client side. Please consult the API documentation for further information.

# Wica Automatic API Documentation

The Wica frontend (JSdoc) and backend (Javadoc) API documentation is built automatically every time a new commit is
pushed to the GitLab repository that hosts this project.

  * [JS Frontend](http://controls_highlevel_applications.gitpages.psi.ch/ch.psi.wica2/js/wica) 
  * [Javadoc Backend](http://controls_highlevel_applications.gitpages.psi.ch/ch.psi.wica2/java)


# Project Changes and Tagged Releases

* See the [CHANGELOG.md](CHANGELOG.md) file for further information.
* See also the project's [Jira Kanban Board](https://jira.psi.ch/secure/RapidBoard.jspa?rapidView=1631)
