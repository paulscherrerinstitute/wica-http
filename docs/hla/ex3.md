# Explore the operations you can do on a Wica-Http Server

### Get the value of a channel

The format is as follows:
```
GET /ca/channels/<channelName>
```

Example: (change 5825 to the port your local server is running on)
```
curl "http://sls20-bd-testsrv.psi.ch:5825/ca/channel/wica:test:counter01"
{"type":"REAL","sevr":"0","ts":"2024-01-19T03:19:51.205571488","val":144.00000000}
```

### Get the value of a channel with additional options

The get value operation supports various options including:
- the timeout in milliseconds.
- the numeric scale to be used when returning a channel value.
- the fields to be returned in the JSON representation. For an EPICS system these can be any of 'val', 'sevr', 'ts'.

The format is as follows:  
```  
GET /ca/channels/<channelName>[?timeout=XXX][&fieldsOfInterest=YYY;ZZZ][&numericScale=N]
```

Example:
```
curl "http://sls20-bd-testsrv.psi.ch:5825/ca/channel/wica:test:counter01?fieldsOfInterest=val&numericScale=3"
{"val":10510.000}
```

### Set the value of a channel

The format is as follows:
```
PUT /ca/channels/<channelName>
Content-Type: text/plain

somevalue
```

Example: 
```
curl -H "Content-Type: text/plain" -X PUT "http://sls20-bd-testsrv.psi.ch:5825/ca/channel/wica:test:counter01" -d 1234

OK
```

### Create a stream, get the returned stream ID

```
POST http://localhost:8080/ca/streams
Content-Type: application/json

{ "channels" : [ { "name": "wica:test:counter01", "props": { "daqmode": "monitor"  } },
                 { "name": "wica:test:counter02", "props": { "daqmode": "poll", "pollint": "5000" } }],
  "props" : { "prec": 2, "hbflux": "8000", "monflux": 1000, "pollflux": 2000 }
}
```

Example:
```
curl -H "Content-Type: application/json"  "http://sls20-bd-testsrv.psi.ch:5825/ca/streams" -d '{ "channels": [ { "name": "wica:test:counter01" } ] }' 
22
```

### Subscribe to a stream

Example:
```
curl "http://sls20-bd-testsrv.psi.ch:5825/ca/streams/22
```

Response:
```
id:22
event:ev-wica-channel-value
data:{"wica:test:counter01":[{"sevr":"0","val":12350.00000000}]}
:2024-01-19 04:06:39.106 - channel monitored values

id:22
event:ev-wica-channel-value
data:{"wica:test:counter01":[{"sevr":"0","val":12351.00000000}]}
:2024-01-19 04:06:39.206 - channel monitored values

etc
```

