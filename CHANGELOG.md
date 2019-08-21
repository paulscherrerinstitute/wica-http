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

* [1.0.0-RELEASE](https://git.psi.ch/controls_highlevel_applications/ch.psi.wica2/tags/1.0.0-RELEASE)
  This is the first production release.  It now runs on OpenJdk11. 131 unit tests passed. The PSI library ca tests
  are currently disabled as they should not really be part of the Wica project.
   - [CTRLIT-6994](https://jira.psi.ch/browse/CTRLIT-6994): Create First Production 1.0.0-Release
   - [CTRLIT-6620](https://jira.psi.ch/browse/CTRLIT-6620): Add first support for JS and Java API docs.
   - [CTRLIT-6901](https://jira.psi.ch/browse/CTRLIT-6901): Address issues raised by Simon during recent tests.
   
* [1.0.1-RELEASE](https://git.psi.ch/controls_highlevel_applications/ch.psi.wica2/tags/1.0.1-RELEASE)
   - [CTRLIT-7103](https://jira.psi.ch/browse/CTRLIT-7103): Fix 1.0.0 Regression Bug.
   - [CTRLIT-7104](https://jira.psi.ch/browse/CTRLIT-7104): Create 1.0.1-RELEASE.

* [1.1.0-RELEASE](https://git.psi.ch/controls_highlevel_applications/ch.psi.wica2/tags/1.1.0-RELEASE)
First release which supports new embryonic feature: channel GET and PUT operations..  
   - [CTRLIT-7105](https://jira.psi.ch/browse/CTRLIT-7108): Make status panels available in PSI's demilitarized zone.
   - [CTRLIT-7108](https://jira.psi.ch/browse/CTRLIT-7108): Add support to the REST service to support channel access GET and PUT.
   - [CTRLIT-7110](https://jira.psi.ch/browse/CTRLIT-7104): Create 1.1.0-RELEASE.

* [1.2.0-RELEASE](https://git.psi.ch/controls_highlevel_applications/ch.psi.wica2/tags/1.2.0-RELEASE)
   - [CTRLIT-7142](https://jira.psi.ch/browse/CTRLIT-7142): Create 1.2.0-RELEASE which supports loading JSON5 library 
   as module rather than using script tag. Also wica Custom Event generation is now enabled.

* [1.3.0-RELEASE](https://git.psi.ch/controls_highlevel_applications/ch.psi.wica2/tags/1.3.0-RELEASE)
  Supports new X-WICA_ERROR header. Delete Stream bug now fixed. See "Project Ideas Completed in Latest Release"
  for more information.
   - [CTRLIT-7199](https://jira.psi.ch/browse/CTRLIT-7199): Create 1.3.0-RELEASE. 

* [1.4.0-RELEASE](https://git.psi.ch/controls_highlevel_applications/ch.psi.wica2/tags/1.4.0-RELEASE)
   - [CTRLIT-7216](https://jira.psi.ch/browse/CTRLIT-7216): Create 1.4.0-RELEASE. 
   - Big structural refactoring to separate stream and channel into separate packages.
   - Added support for configuring PSI CA library.
   - Upgraded to latest SpringBoot release (2.1.6)
   - Added logging performance improvements (used async loggers) and configurable log test on startup.
   - Added builders for WicaStream, WicaStreamproperties and WicaChannel.
   - Communication with the underlying control system is now completely decoupled and uses SpringBoot
     event system.
* [1.5.0-RELEASE](https://git.psi.ch/controls_highlevel_applications/ch.psi.wica2/tags/1.5.0-RELEASE)
    - [CTRLIT-7232](https://jira.psi.ch/browse/CTRLIT-7232): Create 1.5.0-RELEASE. 
    - Continuation of the big refactoring cleanup. This release now supports poll of monitor channel
      and direct poll of remote IOC. At rate configured on a per channel basis by new pollint parameter.
    - Fixed a regression problem with the filtering service which meant that the deadband and fixed
      cycle length filters were not working properly.
    - Cleaned up stream configuration seralisation and deserialisation using the newer Jackson library
      mixin strategy. Probably the WicaChannelData should also be cleaned up. 
    - Fixed a problem with polling whereby different streams could not use different poll rates for
      the same underlying control varaiable.
    - Fixed bug whereby polling operation was setting metadata to UNKNOWN.    
    - Added JDWP support so that the live application can be debugged inside a Docker container. 

* [1.6.0-RELEASE](https://git.psi.ch/controls_highlevel_applications/ch.psi.wica2/tags/1.6.0-RELEASE)
   - [CTRLIT-7235](https://jira.psi.ch/browse/CTRLIT-7235): Create 1.6.0-RELEASE. 
    - Better support for polling (both direct) and on monitors. Polling channels are now shared when
      the polling interval and channel name are the same.
    - Wica Channel Get controller can now return data of varied type. Fixed bug whereby numeric
      precision did not work (because all channels were considered Strings).
    - Added possibility to specify fields of interest to be returned by Wica Channel Get controller.
    - Wica streams no longer provide back history from when the channel was created. Each new subscriber
      received the last available value plus all values which subsequently arrive.
    - further code cleanup and work on making the unit tests more robust.   
    
* [1.7.0-RELEASE](https://git.psi.ch/controls_highlevel_applications/ch.psi.wica2/tags/1.7.0-RELEASE)
   - [CTRLIT-7469](https://jira.psi.ch/browse/CTRLIT-7469): Create 1.7.0-RELEASE.
   - This release contains a performance optimisation so that Jackson Object Mappers are cached and
     reused rather than created on each notification. 
   - The WicaChannelValue, WicaChannelMetadata and WicaChannelData classes have been reworked so
     that they are pure immutable model elements; serialisation is now achieved using the same
     Mixin approach that has now been adopted for the stream configuration.  
   - The default stream parameters sent by the JS component have now been modified to reduce the
     demands on the browser. The new default monitor flux update rate is 200ms (reduced from 100ms), 
     the default numeric precision is now 3 digits (reduced from 6). 
   - The above changed were verified by a corresponding increase in the number of unit tests.
   - Added support for JMX debugging inside docker container. But turned off for now.
   - Added .pom file support for running using Undertow embedded server. But turned off for now. 
 
 
# Project Ideas Completed in Latest and Earlier Releases

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
1. Optimisation: WICA clients that connect for the first time should receive the full list of channels with current 
  values. They should then only be advised of CHANGES to the value. DONE.
1. Cleanup message id's to support the following:
     - metadata-stream: Sends all values with SLOW periodicity. DONE (SENT ONCE)
     - value-snapshot-stream: Sends all values with SLOW periodicity. DONE (VIA POLLING MECHANISM)
     - value-change-stream: Sends only changed values but with fast periodicity.  DONE.
1. Add support to the REST service to support channel access GET and PUT. COMPLETED (BUT PERFORMANCE COULD BE IMPROVED).
1. BUG FIX: delete stream operation now removes id from map so stream cannot be deleted more than once.
1. BUG FIX: Shutdown method now correctly signals subscribers that the stream has been disposed of.
1. ENHANCEMENT: Improved Javadoc.
1. ENHANCEMENT: Now supports X-WICA-ERROR header to consistently describe the cause of any failures.
1. ENHANCEMENT: improved Spring documentation on configurable parameters.
1. ENHANCEMENT: Added support for more thorough reactive flux testing using StepVerifier.
1. ENHANCEMENT: added support for configuring default timeouts for GET and PUT operations and for setting default 
   numeric precision of GET operations.
1. REMOVED: Make status panels available in PSI's demilitarised zone. WILL BE COMPLETED IN WICA RELAY PROJECT.
1. REMOVED: Work out how best to integrate with K8ie's work on SVG. WILL BE COMPLETED IN WICA PANELS PROJECT.
1. Improve the admin page by reporting on the active clients and their IP's. DONE.
1. Improve behaviour when navigating away from Wica Pages. MOSTLY DONE.
1. Run units tests on GitLab server on check in. DONE.
1. Provide automanagement for channel names which share the same epics channel. DONE.
1. Fix bug whereby whereby metadata says type UNKNOWN (causes occasional problem with formatting). FIXED.
1. Create end-to-end tests to measure performance. DONE.

# Project Ideas Backlog

When an idea is under serious consideration and a candidate for
implementation it will be placed on the project's [Jira Kanban Board](https://jira.psi.ch/secure/RapidBoard.jspa?rapidView=1631)

1. Consider switching to http2. Need to resolve bug exposed when client navigates away.
1. Improve page load times by minifying loadable artifacts.
1. Review existing state of Javadoc and Jsdoc and make improvements.
1. Consider a better name.
1. Consider splitting up backend and frontend server into separate Git repositories.
1. Refactor stream-manager to use promises and/or new JS async semantics.
1. Go through all code TODO's and try to resolve them.   
1. Add reporting on test coverage.
1. Organise design review with Simon and/or others.
1. Automate units tests so that we dont have to start an EPICS IOC manually before running the tests.
1. Check validity of all JCIP thread safety annotations.
1. Ability to set stream properties on html page rather than just accept defaults. 
1. Improve diagnostic messages when HTML page contains json content that is not valid.
1. Improve performance of channel GET and PUT.
1. Consider adding the possibility of stream sharing, data value backtrace (eg "backtrace: true")
   and buffer size ("bufsize: 1000"). This would allow us to solve the problem with the ten hours
   trace data needed for the HIPA display.
1. Consider adding a pre-transition point to the change filter.    
1. Consider adding support for explicitly controlling stream properties in html pages.