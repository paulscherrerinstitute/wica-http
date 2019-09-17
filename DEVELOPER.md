# Overview

This page is a random collection of notes on various technical topics that influenced the design of the project.

# Notes on EPICS CA Testing

Wica2 leverages off PSI's in-house commissioned Java Channel Access (ca) library. This is publicly available via 
Github at [this](https://github.com/channelaccess/ca_matlab) location.

Wica2 tries to optimise its use of the library to keep the costs (eg CPU and memory consumption) reasonable. 
To do so the library was tested to assess the cost of various operations. The results were originally discussed 
on [this page](EPICS_TESTS.md). Subsequently a new CA library release has been made and the results will now and
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


# Additional Note on Unit Tests

Note to run the unit tests you newd to start an epics IOC something like this:
~/base-3.14.12.7/bin/darwin-x86/softIoc -d epics_tests.db 