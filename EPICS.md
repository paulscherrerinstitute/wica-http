# WICA support for the EPICS Control System

This document describes the Wica-HTTP Server support for the EPICS Control system.

## Library Dependencies

The Wica-HTTP server is configured to work with PSI's native Java implementation of the [EPICS Control System's](https://epics-controls.org/) 
client-side, channel-access protocol. 

The GitHub project for that library is [here](https://github.com/channelaccess/ca), but since the library is
bundled into the Wica-HTTP Server's fat jar distribution no special measures are required to install it.

## Support for EPICS Channel Access Environment Variables

The Wica-HTTP server respects the normal EPICS conventions with respect to environmental variables, including:

| Property                | Default Value | Desciption |
|-------------------------|---------------| ---------- |
|EPICS_CA_ADDR_LIST       |(empty)        | Address list to use when searching for channels.  |
|EPICS_CA_AUTO_ADDR_LIST  |true           | Automatically build up search address list.       |
|EPICS_CA_CONN_TMO        |30.0           | Disconnect timeout detection interval in seconds. |
|EPICS_CA_BEACON_PERIOD   |15.0           | Rate at which I'm alive beacons will be sent.     |
|EPICS_CA_REPEATER_PORT   |5065           | Channel Access Repeater Listening Port.           |
|EPICS_CA_SERVER_PORT     |5064           | Channel access server port.                       |
|EPICS_CA_MAX_ARRAY_BYTES |1000000        | Maximum size in bytes of an array/waveform.       |
   
For further information see the relevant section of the 
[EPICS Channel Access Reference Manual](https://epics.anl.gov/base/R3-14/12-docs/CAref.html) .  
  
## Support for EPICS Channel Names
  
Wica channel names are made up of three specifiers of the following form:
```
[ <protocol> ] + <csname> + [ '##' + <instance> ]

Where:
  <protocol> - is an optional specifier defining the network protocol.
  <csname> - is a specifier defining the name of the control point as known to the host control system.
  <instance> - is an optional specifier defining the instance. 
```

The *protocol* specifier for EPICS Channel Access is as follows:
```
ca://
```

The *csname* specifier accepts any characters that are valid for EPICS. According to Section 6.3.2 of the EPICS 
Application Developer's Guide these include the following characters:
```
a-z A-Z 0-9 _ - : . [ ] < >
```

The *instance* specifier is only provided to ensure name-uniqueness in a wica stream where a control
system channels is used multiple times.

Examples:
```
   ca://abc:def
   my_special_channel
   my_channel##1
   my_channel##2 
```

## Wica Channel Metadata - mapping to EPICS database fields

The following fields are available for describing the **metadata** of a wica channel whose underlying control system 
is EPICS: 

|Property         |Description                                                                                       |
|-----------------|------------------------------------------------------------------------------------------------  |
| "type"          |One of: "UNKNOWN", STRING", "STRING_ARRAY", "INTEGER", "INTEGER_ARRAY", "DOUBLE", "DOUBLE_ARRAY". |
| "lopr", "hopr"  |The display limits value which were obtained when the channel last came online.                   |
| "drvl", "drvh"  |The drive limits value which were obtained when the channel last came online.                     |
| "lolo", "hihi"  |The error limits value which were obtained when the channel last came online.                     |
| "low", "high"   |The warning limits value which were obtained when the channel last came online.                   |
| "egu"           |The engineering units which was obtained when the channel last came online.                       |
| "prec"          |The numeric precision which was obtained when the channel last came online.                       |

Additional Notes: 
  1. The metadata property fields are initialised using information returned from the underlying EPICS channel in 
     response to a caget request for CTRL information. 
  1. Not all fields will automatically be provided when serializing this information as part of a wica stream. The
     server's [Create Stream](README.md#create-a-wica-stream) request provides control over the payload of 
     what is actually delivered.
    
## Wica Channel Value - mapping to EPICS database fields

The following fields are available for describing the **value** of a wica channel whose underlying control system 
is EPICS: 

|Property       |Description                                                                       |
|---------------|--------------------------------------------------------------------------------- |
| "val"         |The raw value which was obtained when last reading the value of the channel.      |
| "ts"          |The timestamp which was obtained when last reading the value of the channel.      |
| "sevr"        |The alarm severity which was obtained when last reading the value of the channel. |
| "stat"        |The alarm status which was obtained when last reading the value of the channel.   |

Additional Notes: 
  1. the value property fields are initialised using information returned from the underlying EPICS channel in 
     response to a caget request for alarm information. Subsequently, depending on the channel configuration, they 
     are updated using channel-access poll or monitor operations (or when occasionally required, both).
  1. Not all fields will automatically be provided when serializing this information as part of a wica stream. The
     server's [Create Stream](README.md#create-a-wica-stream) request provides control over the payload of 
     what is actually delivered.