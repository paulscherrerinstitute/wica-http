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
  The test.html example has been enhanced to show how to use Wica to perform a caput.
  
  * [Issue #25](https://github.com/paulscherrerinstitute/wica-http/issues/25) Missing java.class.path when loading CA library version 1.3.2 using Java module path.
  * [Issue #26](https://github.com/paulscherrerinstitute/wica-http/issues/26) Create Wica-HTTP Release 1.4.1
  * [Issue #27](https://github.com/paulscherrerinstitute/wica-http/issues/17) Enhance test.html example to show how to perform channel put operation. 
  * [Issue #28](https://github.com/paulscherrerinstitute/wica-http/issues/28) Fix regression bug in docker build.

Test Status: Tests run: 291, Failures: 0, Errors: 0, Skipped: 6  
