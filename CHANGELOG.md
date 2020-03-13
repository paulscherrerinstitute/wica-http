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
  #7: Patch previous (wica-http-1.1.0) release to include newer (ca-1.2.2) library which fixes interoperation with IOC's behind PSI's EPICS Channel Access Gateway
  #8: Patch previous (wica-http-1.1.0) release to include latest (wica-js-1.1.1) wica-JS library.
   
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
  #10 Add averaging filter.
  
  Test Status: Tests run: 334, Failures: 0, Errors: 0, Skipped: 20
  
* [1.4.0] 2020-03-11
  Summary: Miscellaneous Improvements triggered by recent upgrade to Wica-JS release.
  * Issue #11 Upgrade to Spring Boot 2.2.5.RELEASE.
  * Issue #12 Upgrade EPICS CA Library to 1.2.3-dev.
  * Issue #13 Upgrade Wica-JS to 1.2.0.
  * Issue #14 Improve tests.
  * Issue #15 Add support for delete via POST request.
  * Issue #16 Small miscellaneous improvements.
  * Issue #17 Create Wica-HTTP Release 1.4.0
  
  Test Status: Tests run: 340, Failures: 0, Errors: 0, Skipped: 20  