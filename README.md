# Overview

The **Wica2** project is the successor to PSI's earlier [Wica](https://git.psi.ch/controls_highlevel_applications/ch.psi.wica)
project whose stated goal was to provide a: 

> _**"Simple, but powerful, Channel Access to REST service."**_ 

Wica2 includes the goal of the earlier project but expands the vision to target the following primary use case:

> _**"Create some software that will enable an end-user to easily create their own customised web pages that
show the evolving, live status of one or more EPICS channels of interest."**_

Wica2 provides a REST backend service with similar functionality to Wica. Additionally it provides a 
frontend library which can stream live data from the backend, using the received information to dynamically 
update the end-user's web page.

Wica2 is based on technologies that are currently being actively used within PSI's GFA Controls Section. The main 
technology differences between Wica and Wica2 are as follows:

| Original WICA Project                         | WICA2 Project                                           |
| :-------------------------------------------- | :------------------------------------------------------ |
| Backend runs on Glassfish Application Server. | Backend uses JavaSpring Boot containers (Tomcat/Netty). |
| Backend runs directly on linux host.          | Backend runs in Docker container.                       |
| Backend uses EPICS JCA/CAJ CA library.        | Backend uses PSI's EPICS CA client library.             |
| No direct support for frontend (webpages).    | Frontend uses JS library to leverage off modern HTML5 features (eg Server Sent Events (SSE), data-* attributes...)


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
In this example a single channel named "abc:def" is monitored. When the page is loaded the div element's
text content will be dynamically updated with the latest values received from the wica channel.

# How it Works

The principle of operation is as follows. The Wica JS library module is loaded after the rest of the webpage. The 
library scans the document from which it was loaded for elements whose 'data-wica-channel-name' attribute is set.
This attribute is used as a means of indicating that the element is "wica-aware". 

The library then communicates the channel names associated with all wica-aware elements to the Wica REST Server 
(on the backend) which instigates monitoring of the associated data sources and the streaming back of channel 
metadata and value information to the frontend.

In response to the received event stream the Wica JS library module then updates the following attributes of each 
wica-aware html element:

| Attribute                           | Meaning                                                    | Possible Values                                           |
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

In the above example the precision of the channel is set to 0 decimal places.
ToDo: document the supported properties.

# Wica Javascript Client API-v1

The Wica JS library API documentation is [available](https://controls_highlevel_applications.gitpages.psi.ch/ch.psi.wica2/wica/) 
in the Pages section of the GitLab repository.

Note: currently there is a web certificate issue that AIT will work to resolve.

# Wica REST Service API 

## Currently Supported Features 

##### Create Wica Stream

```
POST /ca/streams
Content-Type: application/json

{ "channels" : [ { "name": "abc:def", "props": { "chan_propA": "AAA", "chan_propB": "BBB" } } ], 
     "props" : { "stream_propX": "XXX", "stream_propY": "YYY" } }

Returns <streamId> a unique reference string that can be used when getting the stream (see below).
```

##### Subscribe to Wica Stream
```
GET /ca/streams/<streamId>
```

##### Download the Wica javascript library module:
```
GET /wica/wica.js
```

## New Features Coming Soon (status: 2019-01-14)

##### Get value of a channel
```
GET /ca/channels/<channel>
```

#####  Set the value of channel
```
PUT /ca/channels/<channel>
Content-Type: application/json or text/plain

somevalue
```

# Notes on EPICS CA Testing

Wica2 leverages off PSI's in-house commissioned Java Channel Access (ca) library. This is publicly available via 
Github at [this](https://github.com/channelaccess/ca_matlab) location.

Wica2 tries to optimise its use of the library to keep the costs (eg CPU and memory consumption) reasonable. 
To do so the library was tested to assess the cost of various operations. The results were originally discussed 
on [this page](EPICS_TESTS). Subsequently a new CA library release has been made and the results will now and
in the future be published on the project's [Github site](https://github.com/channelaccess/ca/blob/master/MONITOR_INFO.md)
 
Following this test program the following design decisions have been made:
   * creating contexts is expensive. Therefore everything will be shared
 
# Notes on browser simultaneous connection limits

Tomcat:
Default seems to be 6
Edit with:
about:config ->
network.http.max-persistent-connections-per-server

Chrome:
Default seems to be 6
Edit with (Windows only):
Registry Editor ->
HKEY_CURRENT_USER ->
Software\Microsoft\Windows\CurrentVersion\Internet Settings ->
MaxConnectionsPerServer

# Notes on reactive streams back propagation

See the unit tests

# Project Changes and Tagged Releases

* See the [CHANGELOG.md](CHANGELOG.md) file for further information.
* See also the project's [Jira Kanban Board](https://jira.psi.ch/secure/RapidBoard.jspa?rapidView=1631)


# Notes on WICA Compression

This can be enabled in SpringBoot via the following settings:

server.compression.enabled=true
server.compression.min-response-size=2048
server.compression.mime-types=application/json,application/xml,text/html,text/xml,text/plain,text/event-stream

The SSE data is VERY compressible.

When looking at the PROSCAN Vertical Deflector plot the following compression
was observed (introspection via Wireshark):

Without compression:
Event Stream sent 2.3MB in 300s

With compression:
Wics Event Stream sent 54KB in 300s

Compression factor was 45.

Current Status (2018-09-25): 
-Compression is turned ON.

# Notes on HTTP2

This project explored the possibility of using "h2" (= http version 2) to deliver 
the service.

HowTo:
1.  enable the feature in the application properties file.
1.  build a special version of the openjdk with the necessary ciphers.
1.  define the appropriate ciphers in the application preoperties file.

Advantages:
- all connections are multiplexed over ONE TCP/IP connection. Gets round the
  browser limit on the number of connections per domain.

Disadvantages:
- had to build a special verison of the openjdk which included the cipers 
  needed to handshake with modern browsers.
- when a user navigates away from a webpage a Coyote exception is thrown 
  and all other webpage windows receive a service interruption.   This could
  perhaps be circumvented by sending a close request when navigating away
  from a page.
  
Current Status (2018-09-25):
- For the moment this featured has been turned OFF and the WICA Server is using http v1.2.

# Notes on Character Encoding Issue (ISO8859-1, UTF-8 etc)
  
It was observed that the HIPA status units were not rendering the "µ" character
properly.

Conclusions from investigating:

- EPICS will send whatever we want down the wire.
- the problem comes at the receiving end when we try to build a String again.
- at that moment we need to know whether the respresentation on the wire is ISO8859 or UTF8 or whatever.
- by setting the ‘fileencoding’ JVM option one is able to change the default assumption.
- to fix things for HIPA I switched from UTF8 to ISO8859. But I dont really like doing that: I instinctively feel we should be consistently using  a single encoding scheme (UTF8) throughout our facilities.
- probably we have other things of greater importance to occupy us though !   

Current Status (2018-09-25):
- For the moment I switched the encoding to ISO-8859. 
- This means that the IOC .db files with special ISO8859 characters will by default be rendered correctly.
- Unfortunately this also means that the IOC .db files with special characters encoded in UTF-8 will look strange.

Future Strategy:
- Reverse the decision above and expect byte stream received from IOCs via CA
to be encoded with UTF-8.
- Provide a styling override to allow the units/precision etc to be overridden
  by definitions in the html files.
