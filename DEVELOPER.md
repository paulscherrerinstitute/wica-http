# Wica-HTTP Developer Notes

This page is a miscellaneous collection of notes intended to assist software developers.

## How to Make a New Software Release

1. Use your favourite editor to set the version string in the 'pom.xml' file to the 
   required release number. The release names for this project follow the 
   [semantic versioning](https://semver.org/) naming convention proposed on the GitHub site. 
         
   Examples: 1.0.0, 1.1.0, 1.2.3-rc1, 1.2.3-rc2, 7.1.5-rc19
   
1. Update the [CHANGELOG](CHANGELOG.md) file to describe the new release.

1. Commit locally (- **but don't yet push** -) the latest changes.
    ```
    git commit -m "my latest changes" .
    ```

1. Use the mvn 'release' target to create a tag and to push it to the GitHub Server.
    ```
    mvn run release
    ```
1. Verify that the Travis automatic build worked and/or that the expected artifacts 
   are available on GitHub and on Docker Hub sites.
   
## Notes on EPICS CA Testing

Wica2 leverages off PSI's in-house commissioned Java Channel Access (ca) library. This is publicly available via 
Github at [this](https://github.com/channelaccess/ca) location.

Wica-Http tries to optimise its use of the library to keep the costs (eg CPU and memory consumption) reasonable. To do 
so the library was tested to assess the cost of various operations (eg create context, create channel, add monitor etc). 
The results of these tests were originally published on a markdown page within this project. However, since CA
release 1.3.2 these tests have been integrated into the CA project itself where they can be viewed on the project's 
[Github site](https://github.com/channelaccess/ca/blob/master/INTEGRATION_TESTS.md).
 
Following this test program the following design decisions have been made:
   * creating contexts is relatively expensive. Therefore, their use will be minimised and channels for different 
     Wica clients will all share the same context.
 
## Notes on browser simultaneous connection limits

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

## Notes on reactive streams back propagation

See the unit tests

## Notes on WICA Compression

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

## Notes on HTTP2

This project explored the possibility of using "h2" (= http version 2) to deliver the service.

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

## Notes on Character Encoding Issue (ISO8859-1, UTF-8 etc)
  
It was observed that the HIPA status units were not rendering the "µ" character properly.

Conclusions from investigating:

- EPICS will send whatever we want down the wire.
- the problem comes at the receiving end when we try to build a String again.
- at that moment we need to know whether the representation on the wire is ISO8859 or UTF8 or whatever.
- by setting the ‘fileencoding’ JVM option one is able to change the default assumption.
- to fix things for HIPA I switched from UTF8 to ISO8859. But I dont really like doing that: I instinctively feel we should be consistently using  a single encoding scheme (UTF8) throughout our facilities.
- probably we have other things of greater importance to occupy us though !   

Current Status (2018-09-25):
- For the moment I switched the encoding to ISO-8859. 
- This means that the IOC .db files with special ISO8859 characters will by default be rendered correctly.
- Unfortunately this also means that the IOC .db files with special characters encoded in UTF-8 will look strange.

Future Strategy:
- Reverse the decision above and expect byte stream received from IOCs via CA to be encoded with UTF-8.
- Provide a styling override to allow the units/precision etc to be overridden by definitions in the html files.


## Additional Note on Unit Tests

Note to run the unit tests you newd to start an epics IOC something like this:
~/base-3.14.12.7/bin/darwin-x86/softIoc -d epics_tests.db 
