# Overview

This log describes the functionality of tagged versions within the repository.

# Tags  
* [0.0.1-RELEASE](https://git.psi.ch/controls_highlevel_applications/ch.psi.wica2/tags/0.0.1-RELEASE)
  First version that demonstrates the principle of a monitor on a single epics channel
  which reflects the ongoing status on an HTML page.

* [0.2.0-RELEASE](https://git.psi.ch/controls_highlevel_applications/ch.psi.wica2/tags/0.2.0-RELEASE)
  Created separate Javascript "library". Can now create epics links according to
  data-wica-channel-name attribute appended to html elements.
  Each link is on a separate SSE channel so solution is not in any way scalable !
  !! EVERYTHING IS STILL A TOTAL HACK WHICH NEEDS CLEANING UP !!

* [0.3.0-RELEASE](https://git.psi.ch/controls_highlevel_applications/ch.psi.wica2/tags/0.3.0-RELEASE)
  A cleanup of the first version. Each SSE contains a map of channels with their
  corresponding values. Now supports POST to create a stream and GET to subscribe to it.
  Many clients can therefore subscribe to the same event stream.
  Each stream has its own CA context - this prevents sharing of channels between
  different streams.

   - CTRLIT-6622: Add HTML page to demonstrate features.
   - CTRLIT-6619: Add first support for unit tests.
   - CTRLIT-6624: Rename wica2 to wica.
   - CTRLIT-6618: Upgrade to JDK10.
   - CTRLIT-6623: Clean up application properties file.

* [0.4.0-RELEASE](https://git.psi.ch/controls_highlevel_applications/ch.psi.wica2/tags/0.4.0-RELEASE)
  Continued code organisation cleanup, improved testing etc. 
  Build system switched to maven for consistency with autodeploy project.
  Added support for deployment as a service in docker container on gfa-autodeploy.psi.ch.
  Added support for PROSCAN Status Display demo.
  Can now extract metadata (eg units, precision) from monitored channels.
   - [CTRLIT-6752](https://jira.psi.ch/browse/CTRLIT-6752): Create Release 0.4.0. 
   - [CTRLIT-6753](https://jira.psi.ch/browse/CTRLIT-6753): Improve Test Coverage and Organisation.
   - [CTRLIT-6754](https://jira.psi.ch/browse/CTRLIT-6754): Remove support for JPMS.
   - [CTRLIT-6756](https://jira.psi.ch/browse/CTRLIT-6756): Switch to maven build system.
   - [CTRLIT-6757](https://jira.psi.ch/browse/CTRLIT-6757): Add support for docker deployment.
   - [CTRLIT-6758](https://jira.psi.ch/browse/CTRLIT-6758): Create demo page: PROSCAN Status Display.
   - [CTRLIT-6759](https://jira.psi.ch/browse/CTRLIT-6759): Add support for CORS. 

* [0.5.0-RELEASE](https://git.psi.ch/controls_highlevel_applications/ch.psi.wica2/tags/0.5.0-RELEASE)
  This release supports the first example of a working facility display. The PROSCAN Status display
  - current production version [here](http://gfa-status.web.psi.ch/pro-status.html) - was chosen as an example to 
  see what the issues would be in making a Wica-based html version. The result is [here](https://gfa-autodeploy.psi.ch:8443/demo/ProscanStatusDisplayAdvanced.html) 
  The development philosophy has been: (a) make it work then (b) optimise it. The next wave of development effort will
  now focus more on the latter.
   - [CTRLIT-6771](https://jira.psi.ch/browse/CTRLIT-6771): Create Release 0.5.0. 
   - [CTRLIT-6772](https://jira.psi.ch/browse/CTRLIT-6772): Add support for Wica Index Page. 
   - [CTRLIT-6773](https://jira.psi.ch/browse/CTRLIT-6773): Add support for running EPICS in docker container.  
   - [CTRLIT-6774](https://jira.psi.ch/browse/CTRLIT-6774): Make further improvements to PROSCAN Status Display and other Wica demo pages
   - [CTRLIT-6775](https://jira.psi.ch/browse/CTRLIT-6775): Support locally hosted version of plotly.  
   - [CTRLIT-6777](https://jira.psi.ch/browse/CTRLIT-6777): Add support for gzip compression of SSE Stream.    
   - [CTRLIT-6778](https://jira.psi.ch/browse/CTRLIT-6778): Add support for communicating with EPICS CA Gateways on HIPA, SLS, PROSCAN and SwissFEL.             
   - [CTRLIT-6779](https://jira.psi.ch/browse/CTRLIT-6779): Add support for sending PV precision and units in SSE Stream.   
   - [CTRLIT-6780](https://jira.psi.ch/browse/CTRLIT-6780): Add support for visually communicating between various types of communication failure.      

* [0.6.0-RELEASE](https://git.psi.ch/controls_highlevel_applications/ch.psi.wica2/tags/0.6.0-RELEASE)
  The project starts to mature but is not yet fully stable with various new features in something of an experimental state.
  Main changes are: EPICS channels are now cached, support for delivering metadata to front end, support for rendering
  alarm information, experimental support for http2, now supports deployment at URL: 'https://gfa-wica.psi.ch', initial
  support for delivering value changes.  
   - [CTRLIT-6784](https://jira.psi.ch/browse/CTRLIT-6784): Add support for server main page with basic statistics. 
   - [CTRLIT-6785](https://jira.psi.ch/browse/CTRLIT-6785): Add further support for compression of returned content (eg javascript).
   - [CTRLIT-6786](https://jira.psi.ch/browse/CTRLIT-6786): Add support for sharing EPICS channels between streams.
   - [CTRLIT-6787](https://jira.psi.ch/browse/CTRLIT-6787): Create update to openjdk10.
   - [CTRLIT-6801](https://jira.psi.ch/browse/CTRLIT-6801): Add support for http2.
   - [CTRLIT-6802](https://jira.psi.ch/browse/CTRLIT-6802): Add support for sending channel metadata, alarm and timestamp information.
   - [CTRLIT-6803](https://jira.psi.ch/browse/CTRLIT-6803): Create Release 0.6.0. 
   - [CTRLIT-6805](https://jira.psi.ch/browse/CTRLIT-6805): Switch deployment to 'gfa-wica.psi.ch'.
   - [CTRLIT-6806](https://jira.psi.ch/browse/CTRLIT-6806): Create Initial Display Page for HIPA.

* [0.7.0-RELEASE](https://git.psi.ch/controls_highlevel_applications/ch.psi.wica2/tags/0.7.0-RELEASE)
   Biggest change was switch to CSSS based rendering and cleanup of wica.js file to use ECMA6 classes.
   Reverted http2 -> http, fix to infamous HIPA µ character encoding problem. Many other minor improvements and bug fixes.
   - [CTRLIT-6813](https://jira.psi.ch/browse/CTRLIT-6813): Create 0.7.0 Release.
   - [CTRLIT-6814](https://jira.psi.ch/browse/CTRLIT-6814): Revert http2 to http.
   - [CTRLIT-6815](https://jira.psi.ch/browse/CTRLIT-6815): Improve class naming for greater consistency.
   - [CTRLIT-6816](https://jira.psi.ch/browse/CTRLIT-6816): Improve startup responsiveness.
   - [CTRLIT-6817](https://jira.psi.ch/browse/CTRLIT-6817): Switch to CSS based rendering of WICA components.
   - [CTRLIT-6818](https://jira.psi.ch/browse/CTRLIT-6818): Add threadpool configuration support for Spring mvc task executor.
   - [CTRLIT-6819](https://jira.psi.ch/browse/CTRLIT-6819): Miscellaneous improvements to WICA Display Pages.
   - [CTRLIT-6820](https://jira.psi.ch/browse/CTRLIT-6820): Configure WicaChannelValue serialisation to eliminate data that is currently (timestamps, alarm, state..).
   - [CTRLIT-6821](https://jira.psi.ch/browse/CTRLIT-6821): Upgrade to use latest (Java 8 targeted) PSI CA library (1.2.1).
   - [CTRLIT-6822](https://jira.psi.ch/browse/CTRLIT-6822): Fix heartbeat so that it comes more frequently than timeout.
   - [CTRLIT-6823](https://jira.psi.ch/browse/CTRLIT-6823): Refactor wica library to use ECMA6 classes.
   - [CTRLIT-6824](https://jira.psi.ch/browse/CTRLIT-6824): Set up system to use ISO8859-1 file encoding.  
   - [CTRLIT-6825](https://jira.psi.ch/browse/CTRLIT-6825): Resolve HIPA micro charracter / iso8859-1 character encoding issue.     
   - [CTRLIT-6826](https://jira.psi.ch/browse/CTRLIT-6826): Fix bug whereby only one client was notified of the value change stream.   
   - [CTRLIT-6827](https://jira.psi.ch/browse/CTRLIT-6827): Cleanup MVC Configuration to get rid of deprecated warnings.            
   - [CTRLIT-6831](https://jira.psi.ch/browse/CTRLIT-6831): Correct problem with PROSCAN Status Display when plotting more than 900 points.
   - [CTRLIT-6843](https://jira.psi.ch/browse/CTRLIT-6843): Create Prototypes for HIPA RF Group.    
   - [CTRLIT-6844](https://jira.psi.ch/browse/CTRLIT-6844): Reorganise Wica Demo Pages for better structure.
   - [CTRLIT-6845](https://jira.psi.ch/browse/CTRLIT-6845): Set precision for all fields for compatibility with existing PROSCAN Status Display.
   - [CTRLIT-6846](https://jira.psi.ch/browse/CTRLIT-6846): Create separate Wica Admin Page.
   - [CTRLIT-6847](https://jira.psi.ch/browse/CTRLIT-6847): Upgrade Plotly JS library to latest version.
   - [CTRLIT-6848](https://jira.psi.ch/browse/CTRLIT-6848): Eliminate white flash when connecting by defining CSS transition.
   - [CTRLIT-6849](https://jira.psi.ch/browse/CTRLIT-6849): Further javascript cleanup: create class for WicaRenderingManager.
   - [CTRLIT-6850](https://jira.psi.ch/browse/CTRLIT-6850): Cleanup stream implementation so that it only sends the channel metadata and value maps on first connect.

* [0.8.0-RELEASE](https://git.psi.ch/controls_highlevel_applications/ch.psi.wica2/tags/0.8.0-RELEASE)
   - [CTRLIT-6853](https://jira.psi.ch/browse/CTRLIT-6853): Add support for external application monitoring via Spring Boot Server Admin App.
   - [CTRLIT-6854](https://jira.psi.ch/browse/CTRLIT-6854): Create Software Release 0.8.0
   - [CTRLIT-6855](https://jira.psi.ch/browse/CTRLIT-6854): Perform initial tweaks to address issues raised against RF Display Panels.

* [0.9.0-RELEASE](https://git.psi.ch/controls_highlevel_applications/ch.psi.wica2/tags/0.9.0-RELEASE)
  This feature is an interim rlease on the way towards support caget and caput and various other features.
  The codebease works but thins in the SpringBoot Java area wrt EPICS are in a state of flex and need revisiting to cleanup. 
   - [CTRLIT-6931](https://jira.psi.ch/browse/CTRLIT-6931): Define artifiact naming convention
   - [CTRLIT-6912](https://jira.psi.ch/browse/CTRLIT-6912): Modualarise JS support.
   - [CTRLIT-6877](https://jira.psi.ch/browse/CTRLIT-6877): Remove demo panels and index as these will in the future be supported by separate wica2_panels git repository.
   - [CTRLIT-6853](https://jira.psi.ch/browse/CTRLIT-6853): Add support for external application monitoring via Spring Boot Server Admin App.
   - [CTRLIT-6878](https://jira.psi.ch/browse/CTRLIT-6878): Add support for serving static resources from outside container.
   - [CTRLIT-6959](https://jira.psi.ch/browse/CTRLIT-6959): Miscellaneous improvements. These include: 
                                                            - improved type support and initial attempt at support cagets.
                                                            - admin page is now served via /admin path.
                                                            - first start at supporting caget and caput
                                                            - improved type support for non-scalar channels.
                                                            - Fixed typos.
                                                            - Added separate type for non-scalar channel types: INTEGER_ARRAY, REAL_ARRAY, STRING_ARRAY.
                                                            - Added support for WICA_HOST constant - this should be integrated into the build at some point.
                                                            - fixed typo.
                                                            - The renderer will no longer be called if a channel is offline. 
                                                            - Added support for rendering real or integer numbers that are  NaN and Infinity'. Added better support for PRECISION.
                                                            - improved debug-ability by adding intermediate variable which can take breakpoint.
                                                            - temporarily switched to debian image and openjdk10
                                                            - support for cpu metrics
                                                            - added support for static locations and add extra information to actuator. Add initial get filterT.

* [0.9.5-RELEASE](https://git.psi.ch/controls_highlevel_applications/ch.psi.wica2/tags/0.9.5-RELEASE)
   - [CTRLIT-XXXX](https://jira.psi.ch/browse/CTRLIT-XXXX): XXXX




# Project Ideas Completed

1. Consider refactoring so that the app only uses one context (channels can then be cached and shared between 
different streams). DONE.
1. Add support for different types. DONE.
1. Add support for CA metadata (timestamps, graphics, alarms etc) DONE 
1. Add support for array data. DONE.
1. Add support for plotting using eg plotly. DONE.
1. Optimisation: Currently each time a WICA-supported web page is opened the page will be scanned for data-wica-channel-name
entries and a new monitor will be established for each item. On the PROSCAN status display page there are 88 items.
It would be far more efficient if EPICS channels were cached and shared between the various streams. DONE.
1. Feature Enhancement: add support for displaying alarm values in different colours. DONE.
1. Infrastructure Enhancement: Apply for 'gfa-wica web' certificate. DONE.
1. Bug Fix: sometimes the Wica Server sends disconnect messages but afterwards the monitor continues to send updates
so one is left with a screen with lost of pink background (pink = the Wica server disconnected). DONE.
1. Render changes to server and epics channel connection state using CSS.  DONE. 
1. Provide some example displays for Hubert. DONE.
1. Feature Enhancement: enhance PROSCAN display so that the design is fully responsive (currently it only works well on 
a desktop monitor). DONE.

# Project Ideas Backlog

When an idea is under serious consideration and a candidate for
implementation it will be placed on the project's [Jira Kanban Board](https://jira.psi.ch/secure/RapidBoard.jspa?rapidView=1631)
1. Create end-to-end tests to measure performance.
1. Optimisation: WICA clients that connect for the first time should receive the full list of channels with current 
values. They should then only be advised of CHANGES to the value.

1. Cleanup message id's to support the following:
   - metadata-stream: Sends all values with SLOW periodicity. 
   - value-snapshot-stream: Sends all values with SLOW periodicity. 
   - value-change-stream: Sends only changed values but with fast periodicity.

   