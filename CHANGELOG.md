# Overview

This log describes the functionality of tagged versions within the repository.

# Tags  
* [1.0.0] 
  Created first version based on version 1.7.0-RELEASE of the incubator project on PSI's 
  internal GitLab Server (ch.psi.wica2).
  
  Test Status: Tests run: 279, Failures: 0, Errors: 0, Skipped: 8
  
* [1.0.1] 
  This release is a documentation update. The functionality of the server is unchanged.
  Test Status: Tests run: 279, Failures: 0, Errors: 0, Skipped: 8
    
* [1.1.0] 
  Documentation improvements. Docker build now includes EPICS inside the container.
  Test Status: Tests run: 279, Failures: 0, Errors: 0, Skipped: 8
   
* [1.1.1] 
  Includes patched version of PSI CA library which fixes problem when interoperating.
  with IOC's behind EPICS Channel Access Gateway.
  Now includes EPICS in Docker container. 
  Docker container now moved from Azul Java to AdoptOpenJDK
    
  Issues:
  Issue #7: Patch previous (wica-http-1.1.0) release to include newer (ca-1.2.2) library which fixes interoperation with IOC's behind PSI's EPICS Channel Access Gateway
  Issue #8: Patch previous (wica-http-1.1.0) release to include latest (wica-js-1.1.1) wica-JS library.
   
  Test Status: Tests run: 279, Failures: 0, Errors: 0, Skipped: 8
   
* [1.1.2] 
  Development enhancement: added ability to override JAR file externally.
  Now includes Wica-JS 1.1.2    
  Test Status: Tests run: 279, Failures: 0, Errors: 0, Skipped: 8
   
* [1.3.0] 2020-01-13
  Added support for new averaging filter.    
  Release now bundles wica-js.1.1.3.
  Improvements to EPICS monitor management: (a) faster connect times when operating through gateway; (b) now
  supports configurable delayed release of resources.
  Improvement to statistics information displayed when accessing /admin endpoint.
  Dockerfile build now supports JMX debug. 
  Improvements to Travis automatic build and publication procedure.
  Upgrade to later version of Tomcat (9.0.30) to fix bug (exception generated when navigating away from web pages).

  GitHub Issues Resolved:
  Issue #10 Add averaging filter.
  
  Test Status: Tests run: 334, Failures: 0, Errors: 0, Skipped: 20
  
* [1.4.0](https://github.com/paulscherrerinstitute/wica-http/releases/tag/1.4.0) Released 2020-06-28

  ###### Overview:
  
  Miscellaneous Improvements triggered by recent upgrade to Wica-JS 1.2.2 release.
  
  ###### Change List: 
  
  * [Issue #11](https://github.com/paulscherrerinstitute/wica-http/issues/11) Upgrade to Spring Boot 2.2.5.RELEASE.
  * [Issue #12](https://github.com/paulscherrerinstitute/wica-http/issues/12) Upgrade EPICS CA Library to 1.2.3-dev.
  * [Issue #13](https://github.com/paulscherrerinstitute/wica-http/issues/13) Upgrade Wica-JS to 1.2.0.
  * [Issue #14](https://github.com/paulscherrerinstitute/wica-http/issues/14) Improve tests.
  * [Issue #15](https://github.com/paulscherrerinstitute/wica-http/issues/15) Add support for delete via POST request.
  * [Issue #16](https://github.com/paulscherrerinstitute/wica-http/issues/16) Small miscellaneous improvements.
  * [Issue #17](https://github.com/paulscherrerinstitute/wica-http/issues/17) Create Wica-HTTP Release 1.4.0
  * [Issue #18](https://github.com/paulscherrerinstitute/wica-http/issues/18) Added bug fix to issue which polluted log when navigating away from a wica page. 
  * [Issue #19](https://github.com/paulscherrerinstitute/wica-http/issues/19) Added support for new EPICS configuration properties.
  * [Issue #20](https://github.com/paulscherrerinstitute/wica-http/issues/20) Upgrade Wica-JS to 1.2.1. 
  * [Issue #21](https://github.com/paulscherrerinstitute/wica-http/issues/21) Upgrade EPICS CA library to 1.3.2.
  * [Issue #22](https://github.com/paulscherrerinstitute/wica-http/issues/22) Upgrade Wica-JS to 1.2.2. 
  * [Issue #23](https://github.com/paulscherrerinstitute/wica-http/issues/23) Upgrade code to avoid JsonParser ALLOW_NON_NUMERIC_NUMBERS deprecation.
  * [Issue #24](https://github.com/paulscherrerinstitute/wica-http/issues/24) Add CI support for Jacoco code coverage.
  
  Test Status: Tests run: 291, Failures: 0, Errors: 0, Skipped: 6  
  
  ###### Known Problems:
    
  Unfortunately, the docker build had a problem when loading the new version (1.3.2) of the CA library. Sorry, this 
  functionality will be tested in future releases. Docker users should use version 1.4.1 instead.
  
* [1.4.1](https://github.com/paulscherrerinstitute/wica-http/releases/tag/1.4.1) Released 2020-07-09

  Fixed a regression in the use of the CA library (version 1.3.2) which prevented the Docker build from loading the 
  library properly.
  The test.html example has been enhanced to show how to use Wica to write to a control system channel.
  
  * [Issue #25](https://github.com/paulscherrerinstitute/wica-http/issues/25) Missing java.class.path when loading CA library version 1.3.2 using Java module path.
  * [Issue #26](https://github.com/paulscherrerinstitute/wica-http/issues/26) Create Wica-HTTP Release 1.4.1
  * [Issue #27](https://github.com/paulscherrerinstitute/wica-http/issues/17) Enhance test.html example to show how to perform channel put operation. 
  * [Issue #28](https://github.com/paulscherrerinstitute/wica-http/issues/28) Fix regression bug in docker build.

Test Status: Tests run: 291, Failures: 0, Errors: 0, Skipped: 6  

* [1.5.0](https://github.com/paulscherrerinstitute/wica-http/releases/tag/1.5.0) Released 2020-09-15.

  First implementation of Wica Channel dead monitor detection.
  Now bundles wica-js release 1.3.0 which includes support for multiple streams on same HTML page.
  
  * [Issue #29](https://github.com/paulscherrerinstitute/wica-http/issues/29) Create Wica-HTTP Release 1.5.0-rc1.
  * [Issue #30](https://github.com/paulscherrerinstitute/wica-http/issues/30) Add support for dead monitor detection.
  * [Issue #31](https://github.com/paulscherrerinstitute/wica-http/issues/31) Miscellaneous improvements for release 1.5.0.
  * [Issue #32](https://github.com/paulscherrerinstitute/wica-http/issues/32) Improve Docker naming on wica-related volumes.
  * [Issue #33](https://github.com/paulscherrerinstitute/wica-http/issues/33) Create Wica-HTTP Release 1.5.0.
  

* [1.5.1](https://github.com/paulscherrerinstitute/wica-http/releases/tag/1.5.1) Released 2020-09-20.

  Improvements to Wica Channel Dead Monitor Detector.
  Fix regression bug with Timestamp serialisation
  Now bundles wica-js release 1.3.1.
  
  * [Issue #34](https://github.com/paulscherrerinstitute/wica-http/issues/34) Further improvements to Wica Channel Dead Monitor Detector.
  * [Issue #35](https://github.com/paulscherrerinstitute/wica-http/issues/35) Fix regression bug with Timestamp serialisation
  * [Issue #36](https://github.com/paulscherrerinstitute/wica-http/issues/36) Create Wica-HTTP Release 1.5.1
  * [Issue #37](https://github.com/paulscherrerinstitute/wica-http/issues/37) Bundle Wica-JS release 1.3.1
  
Test Status: Tests run: 336, Failures: 0, Errors: 0, Skipped: 9  


* [1.6.0](https://github.com/paulscherrerinstitute/wica-http/releases/tag/1.6.0) Released 2020-09-21.

  Added support for reloading the control system monitors associated with a Wica Stream.
  A stream can now be reloaded using the command: 'curl -i -X PUT localhost:8080/ca/streams/<streamId>?action=reload'
  
  * [Issue #38](https://github.com/paulscherrerinstitute/wica-http/issues/34) Create Wica-HTTP Release 1.6.0.
  * [Issue #39](https://github.com/paulscherrerinstitute/wica-http/issues/35) Add support for restarting the control system monitors associated with a Wica Channel.
  * [Issue #40](https://github.com/paulscherrerinstitute/wica-http/issues/36) Logging Improvements.
  * [Issue #41](https://github.com/paulscherrerinstitute/wica-http/issues/37) Small Miscellaneous Improvements for Wica-HTTP Release 1.6.0.
  
Test Status: Tests run: 349, Failures: 0, Errors: 0, Skipped: 9  
 
* [1.7.0](https://github.com/paulscherrerinstitute/wica-http/releases/tag/1.7.0) Released 2020-10-04.
 
  Main improvement was refactoring of support for EPICS types. Now properly handles scalar and array
  DBR types associated with BYTE, FLOAT and SHORT. Amazingly none of the DB's supported at PSI so far
  were using these types so the discovery that some of this was not working correctly comes late !
  
  The above change was triggered by investigation into use of Wica for streaming low bandwidth image
  data. Thanks to Sarat Raj for the suggestion.
  
  Refactored test database functionality, separating DB used for streaming ('counter.db'/'counter.html')
  from that used for verifying EPICS types ('types.db'/'types.html'). (Note: the 'types.db' only runs under
  EPICS 7 SoftIOC due to use of waveform initialisation feature).
  
  * [Issue #42](https://github.com/paulscherrerinstitute/wica-http/issues/42) Add wica favicon icon.
  * [Issue #43](https://github.com/paulscherrerinstitute/wica-http/issues/43) Create Wica HTTTP Release 1.6.0-rc1
  * [Issue #44](https://github.com/paulscherrerinstitute/wica-http/issues/44) Add support for handling EPICS channels of DBR_SHORT data type.
  * [Issue #45](https://github.com/paulscherrerinstitute/wica-http/issues/45) Further improvements to EPICS data type support.
  * [Issue #46](https://github.com/paulscherrerinstitute/wica-http/issues/46) Provide test database for verifying transport of all EPICS types.
  * [Issue #47](https://github.com/paulscherrerinstitute/wica-http/issues/47) Create Wica HTTP Release 1.7.0.
      
Test Status: Tests run: 349, Failures: 0, Errors: 0, Skipped: 9  
 
 
* [1.8.0](https://github.com/paulscherrerinstitute/wica-http/releases/tag/1.8.0) Released 2020-11-16.

Some bug fixes and enhancements mostly centred around the need to efficiently support streaming waveform records containing camera data.

Behaviour change: if the stream consumer cannot keep up with the SSE event flux then data is now dropped and a warning message placed in
the server log. Previously the stream would be destroyed on the server and the client was forced to resubscribe.

Enhancement: a new boolean stream property has been defined called quietMode (current default is false). When set true then the stream only sends
polled and/or monitored value update messages when there is new information. Previously the messages would be sent but with zero payload.

Behaviour change: channels whose data acquisition mode is set to poll will now also acquire and publish channel metadata when it
comes online.

Behaviour change: channels whose data acquisition mode is set to poll will only do so when the channel is online. 

  * [Issue #48](https://github.com/paulscherrerinstitute/wica-http/issues/48) BUG FIX: Fix incorrect logger names.
  * [Issue #49](https://github.com/paulscherrerinstitute/wica-http/issues/49) BUG FIX: Fix regression bug whereby poll-only streams were no longer working.
  * [Issue #50](https://github.com/paulscherrerinstitute/wica-http/issues/50) BUG FIX: Update poll-monitor mode so that monitored values do not get published on Wica Stream.
  * [Issue #51](https://github.com/paulscherrerinstitute/wica-http/issues/51) ENHANCEMENT: Further polling improvements. Streams which poll now also publish channel metadata.
  * [Issue #52](https://github.com/paulscherrerinstitute/wica-http/issues/52) ENHANCEMENT: Improved type support in test database and viewer.
  * [Issue #53](https://github.com/paulscherrerinstitute/wica-http/issues/53) ENHANCEMENT: Improve Javadoc on data-acquisition modes.
  * [Issue #54](https://github.com/paulscherrerinstitute/wica-http/issues/54) ENHANCEMENT: Added support for back pressure handling. Now drops data if stream consumer can't keep up.
  * [Issue #55](https://github.com/paulscherrerinstitute/wica-http/issues/55) ENHANCEMENT: Added support for new WicaStreamProperty "quietMode".
  * [Issue #57](https://github.com/paulscherrerinstitute/wica-http/issues/57) Create Wica HTTP Release 1.8.0
  * [Issue #58](https://github.com/paulscherrerinstitute/wica-http/issues/58) Remove deprecated properties "pollratio" and "heartbeat".
  * [Issue #59](https://github.com/paulscherrerinstitute/wica-http/issues/59) ENHANCEMENT: Improvements to Admin Page.
   
Test Status: Tests run: 360, Failures: 0, Errors: 0, Skipped: 13    


* [1.9.0](https://github.com/paulscherrerinstitute/wica-http/releases/tag/1.9.0) Released 2020-12-03.

This was a big refactoring of the aspects of this project that provides support for the EPICS Control System.
The intention was not to introduce breaking behaviour changes but to refactor the structure for improved future
maintainability. 

This release bundles Wica_JS 1.4.0 which supports autogeneration of the Wica Channel instance specifiers and
a feature which allows CSS autoload to be suppressed.

  * [Issue #60](https://github.com/paulscherrerinstitute/wica-http/issues/60) ENHANCEMENT: Refactor EPICS Control System support for improved structure and maintainability.
  * [Issue #61](https://github.com/paulscherrerinstitute/wica-http/issues/61) Create Wica HTTP Release 1.9.0
  * [Issue #62](https://github.com/paulscherrerinstitute/wica-http/issues/62) ENHANCEMENT: Add support for 'wica.channel-publish-poller-restart', 'wica.channel-publish-channel-value-initial-state' 
                                                                              and 'wica.channel-publish-channel-metadata-initial-state' configuration settings/ 
  * [Issue #63](https://github.com/paulscherrerinstitute/wica-http/issues/63) ENHANCEMENT: Retire 'wica.epics-get-channel-value-on-monitor-connect' and 'wica.epics-get-channel-value-on-monitor-connect' configuration settings.
  * [Issue #64](https://github.com/paulscherrerinstitute/wica-http/issues/64) BUG FIX: Commit Docker directory bug
  * [Issue #65](https://github.com/paulscherrerinstitute/wica-http/issues/65) BUG FIX: Fixed problem in poller/monitor lag calculator.
  * [Issue #66](https://github.com/paulscherrerinstitute/wica-http/issues/66) ENHANCEMENT: Improve stream WicaStreamBuilder debug messages. 
  * [Issue #67](https://github.com/paulscherrerinstitute/wica-http/issues/67) BUG FIX: Remove all references to deprecated pollratio
  * [Issue #68](https://github.com/paulscherrerinstitute/wica-http/issues/68) ENHANCEMENT: Bundle Wica-JS release 1.4.0.
  * [Issue #69](https://github.com/paulscherrerinstitute/wica-http/issues/69) ENHANCEMENT: Add Data Acquisition Mode test page. 
  * [Issue #70](https://github.com/paulscherrerinstitute/wica-http/issues/70) ENHANCEMENT: Create counter test database with monitor deadband.
    
Test Status: Tests run: 371, Failures: 0, Errors: 0, Skipped: 14    

* [1.9.1](https://github.com/paulscherrerinstitute/wica-http/releases/tag/1.9.1) Released 2020-12-05.
 
Now bundles latest Wica-JS release 1.5.1. 
 
   * [Issue #71](https://github.com/paulscherrerinstitute/wica-http/issues/71) Bundle Wica-JS release 1.5.1
   * [Issue #72](https://github.com/paulscherrerinstitute/wica-http/issues/72) Create Wica HTTP Release 1.9.1
   
Test Status: Tests run: 371, Failures: 0, Errors: 0, Skipped: 14

* [1.10.0](https://github.com/paulscherrerinstitute/wica-http/releases/tag/1.10.0) Released 2020-12-09.

Upgraded to Spring Boot 2.4.0. 
In future will need to deal with deprecated ReplayProcessor class and Web Client exchange methods. 
Now logs messages to show when system is under back pressure. 
Fixes bug which causes long configuration strings to exceed HTTP header length when invalid.

Application.property files changes: 
  * (a) spring.resources.static.locations -> spring.web.resources.static.locations
  * (b) spring.resources.cache.cachecontrol.max-age -> spring.web.resources.cache.cachecontrol.max-age
  * (c) wica.cors-allowed-origins -> wica.cors-allowed-origin-patterns
    
Issues Addressed:

* [Issue #73](https://github.com/paulscherrerinstitute/wica-http/issues/73) Create Wica-HTTP Release 1.10.0
* [Issue #74](https://github.com/paulscherrerinstitute/wica-http/issues/74) ENHANCEMENT: Insert warnings in log when data is being dropped due to back pressure.
* [Issue #75](https://github.com/paulscherrerinstitute/wica-http/issues/75) BUG FIX: Truncate exception message length when stream configuration string is too long (avoids HTTP header overflow).
* [Issue #76](https://github.com/paulscherrerinstitute/wica-http/issues/76) ENHANCEMENT: Upgrade to Spring Boot 2.4.0.

IntelliJ Test Report: Tests run: 371, Failures: 0, Errors: 0, Skipped: 14

* [1.10.1](https://github.com/paulscherrerinstitute/wica-http/releases/tag/1.10.1) Released 2020-12-11

Fixes the cross origin regression bug reported by Jose.
At the same time reenabled Tomcat to work with the default version for the SpringBoot 2.4.0 platform.

Issues Addressed:

* [Issue #77](https://github.com/paulscherrerinstitute/wica-http/issues/77) Remove override of Tomcat version in pom file.
* [Issue #78](https://github.com/paulscherrerinstitute/wica-http/issues/78) Create Wica-HTTP Release 1.10.1
* [Issue #79](https://github.com/paulscherrerinstitute/wica-http/issues/79) Fix Cross-Origin Regression Bug following move to Spring Boot 2.4.0.

IntelliJ Test Report: Tests run: 371, Failures: 0, Errors: 0, Skipped: 14

* [1.11.0](https://github.com/paulscherrerinstitute/wica-http/releases/tag/1.11.0) Released 2022-04-03

This release brings the wica-http server up-to-date to following evolutions in Spring Boot
and Java. The release does not add any functional changes.

Despite having wica built Wica for Java 17 it was eventually decided to retain Java 11
support for a while longer since PSI's Java systems are still running by default on Java 8.
However, the JVM bundled into the docker release has been updated to Java 17.

Main changes are as follows:
- Travis build system is retired and replaced with GitHub Actions.
- The Java docker build was migrated to Java version 17 but the fatJar continues to be targeted for Java 11 JVM's.
- Spring Boot updated to 2.6.6
- Updated bundled EPICS in docker container to 7.0.6.1
- Updated bundled Wica-JS to 1.5.4

Issues Addressed:

* [Issue #80](https://github.com/paulscherrerinstitute/wica-http/issues/80) Add support for GitHub Actions.
* [Issue #81](https://github.com/paulscherrerinstitute/wica-http/issues/81) Add experimental proxy server
* [Issue #82](https://github.com/paulscherrerinstitute/wica-http/issues/82) Update Docker container to Java 17
* [Issue #83](https://github.com/paulscherrerinstitute/wica-http/issues/83) Update to Spring Boot 2.6.6 and other dependencies to latest
* [Issue #84](https://github.com/paulscherrerinstitute/wica-http/issues/84) Update javadoc.
* [Issue #84](https://github.com/paulscherrerinstitute/wica-http/issues/84) Dockerfile: switch to eclipse-temurin:17 and reduce image size by only copying EPICS binaries 
                                                                            and libs rather than complete installation tree.
* [Issue #85](https://github.com/paulscherrerinstitute/wica-http/issues/85) Eliminate experimental proxy server ready for release.
* [Issue #86](https://github.com/paulscherrerinstitute/wica-http/issues/86) Update README.
* [Issue #87](https://github.com/paulscherrerinstitute/wica-http/issues/87) Create release 1.11.0
* [Issue #88](https://github.com/paulscherrerinstitute/wica-http/issues/88) Add Example Screenshots
* [Issue #89](https://github.com/paulscherrerinstitute/wica-http/issues/89) Bundle Wica-JS 1.5.4

IntelliJ Test Report: Tests run: 371, Failures: 0, Errors: 0, Skipped: 12


* [1.12.0](https://github.com/paulscherrerinstitute/wica-http/releases/tag/1.12.0) Released 2022-09-14
This release brings the wica-http server up-to-date to following evolutions in Spring Boot and Java. 
The release does not add any functional changes.
The source code starts to make use of Java 17 features such as records and new switch.

Main changes are as follows:
- The fatJar has been updated to target the Java 17 JVM which has long term support.
- Spring Boot updated to 2.7.3
- Updated bundled EPICS in docker container to 7.0.7.

Issues Addressed:
* [Issue #90](https://github.com/paulscherrerinstitute/wica-http/issues/90) Add support for direct use of PEM-encoded web certificate and private key. Retire use of Java KeyStore.
* [Issue #91](https://github.com/paulscherrerinstitute/wica-http/issues/91) Make miscellaneous small improvements.
* [Issue #92](https://github.com/paulscherrerinstitute/wica-http/issues/92) Now bundles EPICS base 7.0.7.
* [Issue #93](https://github.com/paulscherrerinstitute/wica-http/issues/93) Upgrade Spring Boot to latest 2.7.3
* [Issue #94](https://github.com/paulscherrerinstitute/wica-http/issues/94) Update Java run time to version 17.
* [Issue #95](https://github.com/paulscherrerinstitute/wica-http/issues/95) Improve Javadoc.
* [Issue #96](https://github.com/paulscherrerinstitute/wica-http/issues/96) Leverage of Java 17 features (records and new switch construct).
* [Issue #97](https://github.com/paulscherrerinstitute/wica-http/issues/97) Update project dependencies to later versions.
* [Issue #98](https://github.com/paulscherrerinstitute/wica-http/issues/98) Update GitHub Action workflow.
* [Issue #99](https://github.com/paulscherrerinstitute/wica-http/issues/99) Create release 1.12.0
* 
IntelliJ Test Report: Tests run: 371, Failures: 0, Errors: 0, Skipped: 12


* [1.12.1](https://github.com/paulscherrerinstitute/wica-http/releases/tag/1.12.1) Released 2022-11-07
This is a bug fix release to address the issue of the disappearing admin page. At the same time the Spring Boot has been upgraded to 2.7.5.

Issues Addressed:
* [Issue #100](https://github.com/paulscherrerinstitute/wica-http/issues/100) BUG FIX: fix regression bug - disappearance of admin page.
* [Issue #101](https://github.com/paulscherrerinstitute/wica-http/issues/101) CHORE: Update to Spring Boot 2.7.5
* [Issue #102](https://github.com/paulscherrerinstitute/wica-http/issues/102) CHORE: Create release 1.12.1
* [Issue #103](https://github.com/paulscherrerinstitute/wica-http/issues/103) DOC: Add talk from CERN GUI Workshop Autumn 2022

IntelliJ Test Report: Tests run: 371, Failures: 0, Errors: 0, Skipped: 12


* [1.12.2](https://github.com/paulscherrerinstitute/wica-http/releases/tag/1.12.1) Released 2022-11-20
  This is a bug fix release to deal with the fact that the Docker build uses the https configuration which will not start without a
  certificate file. This new release uses the http configuration.

Issues Addressed:
* [Issue #104](https://github.com/paulscherrerinstitute/wica-http/issues/104) CHORE: Create release 1.12.2
* [Issue #105](https://github.com/paulscherrerinstitute/wica-http/issues/105) BUG FIX: the default docker build now builds the http version not the https version

IntelliJ Test Report: Tests run: 371, Failures: 0, Errors: 0, Skipped: 12
