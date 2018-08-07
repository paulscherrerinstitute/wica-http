**Note: the information below is under construction and still evolving !**


# Overview

This project is intended to be a successor to PSI's earlier [Wica](https://git.psi.ch/controls_highlevel_applications/ch.psi.wica)
project whose stated goal was to provide:

> *a very simple, but powerful, Channel Access to REST service*

Wica2 aims to provide similar functionality but the vision of the project is stated in rather more abstract terms:

> _**create a mechanism to enable an end-user to easily put together a webpage which
monitors the live status of one or more EPICS channels of interest**_

Wica2 will leverage off up-and-coming technologies within PSI's GFA Controls Section. More concretely the main 
technology differences between Wica and Wica2 are as follows:

| Original WICA Project                | WICA2 project                              |
| :----------------------------------- | :----------------------------------------- |
| Runs on Glassfish Application Server | Uses Spring Boot containers (tomcat/netty) |
| Runs directly on linux host          | Runs in Docker container                   |
| No direct support for HTML           | Uses HTML5 features eg Server Sent Events (SSE) and Global Data (data-*) attributes.
| Uses EPICS JCA/CAJ CA library        | Uses PSI's EPICS CA client library         |


# Wica Webpage

The simplest Wica webpage would look something like this:
```
<!DOCTYPE html>
<html lang="en">
<head>
   <meta charset="UTF-8"/>
   <title>My Awesome Epics Channel Viewer</title>
   <script type="text/javascript" src="gfa-wica.psi.ch/wica.js"></script>
</head>

<body>
   <div data-epics-ca="abc:def:some_channel_of_interest"></div>
</body>

</html>
```

When the page is loaded the underlying Javascript library will analyse the page for elements whose 'data-epics-ca' 
attribute indicates an interest in some EPICS channel. The library then communicates with the wica server which will 
use EPICS Channel Access (CA) technology to monitor the relevant channels and send value updates using the HTML5 Server 
Sent Event (SSE) feature. 


# Wica REST API (Endpoints)

"Underneath the hood" the Wica javascript library communicates with Wica HTTP Server using a series of REST endpoints.
In the short to medium term it is anticipated that the Wica2 server will use an API that is functionally identical to 
the old Wica server whose API is documented [here](https://git.psi.ch/controls_highlevel_applications/ch.psi.wica/blob/master/Readme.md#API)

## Currently Supported Features (status: 2018-08-02)

##### Register new stream (register channels to monitor):
```
POST /ca/streams
Content-Type: application/json

["channel1", "channel2", "channnel3"]
```

##### Subscribe for stream (Server Sent Event) updates:
```
GET /ca/streams/<channelName>
```

##### Download the Wica javascript library:
```
GET /wica.js
```

## New Features Coming Soon (status: 2018-08-02)

##### Get value of a channel
```
GET /ca/channels/<channel>
```

#####  Set value of channel
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
* See also the project's [Jira Kanban Board](https://jira.psi.ch/secure/RapidBoard.jspa?rapidView=1729)

