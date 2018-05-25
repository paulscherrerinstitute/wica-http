# Overview

This log describes the functionality of tagged versions within the repository.

# Tags  
* [0.1.0](https://git.psi.ch/controls_highlevel_applications/ch.psi.wica2/tags/0.1.0)
  First version that demonstrates the principle of a monitor on a single epics channel
  which reflects the ongoing status on an HTML page.

* [0.2.0](https://git.psi.ch/controls_highlevel_applications/ch.psi.wica2/tags/0.2.0)
  Created separate Javascript "library". Can now create epics links according to
  data-epics-channel attribute appended to html elements.
  Each link is on a separate SSE channel so solution is not in any way scalable !

* [0.3.0](https://git.psi.ch/controls_highlevel_applications/ch.psi.wica2/tags/0.3.0)
  A cleanup of the first version. Each SSE contains a map of channels with their
  corresponding values. Now supports POST to create a stream and GT to subscribe to it.
  Many clients can therefore subscribe to the same event stream.
  Each stream has its own CA context - this prevents sharing of channels between
  different streams.

    CTRLIT-6622: Add HTML page to demonstrate features.
    CTRLIT-6619: Add first support for unit tests.
    CTRLIT-6624: Rename wica2 to wica.
    CTRLIT-6618: Upgrade to JDK10.
    CTRLIT-6623: Clean up application properties file.


# Project Ideas Backlog

When an idea is under serious consideration and a candidate for
implementation it will be placed on the project's [Jira Kanban Board](https://jira.psi.ch/secure/RapidBoard.jspa?rapidView=1631)

1. Create end-to-end tests to measure performance.
1. Consider refactoring so that the app only uses one context (channels
   can then be cached and shared between different streams).
1. Add support for different types.
1. Add support for CA metadata (timestamps, graphics,
alarms etc)
1. Add support for array data.
1. Add support for plotting using eg plotly.

