**Note: the information below is still under construction and still evolving !**


# Overview

This project is intended (eventually !) to be a successor to PSI's earlier [wica](https://git.psi.ch/controls_highlevel_applications/ch.psi.wica)
project whose stated goal was to provide for the EPICS collaboration a:

> *very simple, but powerful, Channel Access to REST service*

This new project aims to provide similar functionality but to leverage off up-and-coming
technologies of increasing strategic importance within PSI's GFA Controls Section.

The main vision of wica2 is to

> _**create a mechanism to enable an end-user to easily put together a webpage which
monitors the live status of one or more EPICS channels of interest**_

More concretely the main technology differences between wica and wica2 are as follows:

| Original WICA Project                | WICA2 project                              |
| :----------------------------------- | :----------------------------------------- |
| Runs on Glassfish Application Server | Uses Spring Boot containers (tomcat/netty) |
| Runs directly on linux host          | Runs in Docker container                   |
| No direct support for HTML           | Uses HTML5 features eg Server Sent Events (SSE) and Global Data (data-*) attributes.
| Uses EPICS JCA/CAJ CA library        | Uses PSI's EPICS CA client library         |


# Old Wica API

For reference purposes the old Wica API is available [here](https://git.psi.ch/controls_highlevel_applications/ch.psi.wica/blob/master/Readme.md#API)


# Wica2 HTML5 Webpage

The simplest Wica2 webpage would look something like this:
```
<!DOCTYPE html>
<html lang="en">
<head>
   <meta charset="UTF-8"/>
   <title>My Awesome Epics Channel Viewer</title>
   <script type="text/javascript" src="gfa-wica.psi.ch/js"></script>
</head>

<body>
   <div data-epics-ca="abc:def:some_channel_of_interest"></div>
</body>

</html>
```

When the page is loaded the wica server will create a monitor on
the EPICS channel of interest and send evolving updates which will be
rendered as the text content of the div.

# Wica 2 Endpoints

Register new stream (register channels to monitor)

```
POST ca/streams
Content-Type: application/json

["channel1", "channel2", "channnel3"]
```

Subscribe for stream updates

```
GET ca/streams/<id>
```


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
