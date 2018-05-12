# Overview

This project is intended (eventually ! to be a successor to PSI's earlier 
[wica](ch.psi.wica) project that will leverage off up-and-coming technologies
which are currently gaining currency within PSI's GFA Controls Section.

More concretely the main technology changes are as follows:

   * Glassfish Application Server -> Spring Boot
   * EPICS JCA/CAJ CA library -> PSI's in-house EPICS CA library
   * Standalone server -> runs in Docker container
   
# API

The API has not changed with respect to the [wica](ch.psi.wica) predecessor 
project. The following API description has been "stolen" (= copy and pasted) 
directly.

Get value of a channel

```
GET /ca/channels/<channel>
```

Set value of channel

```
PUT /ca/channels/<channel>
Content-Type: application/json or text/plain

somevalue
```


Register new stream (register channels to monitor)

```
POST ca/streams
Content-Type: application/json

["channel1", "channel2", "channnel3"]
```

Subscribe for stream

```
GET ca/streams/<id>
``` 

It is a two step process because SSE streams seems to be just only fully supported (Server/Browser side) for GET requests.
Once the stream connection closes the also the channels are destroyed.
