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