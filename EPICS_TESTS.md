# Overview

These notes present the results of testing PSI's in-house commissioned [Java ca library](https://github.com/channelaccess/ca_matlab) 
(ie the one developed by CosyLab). The library has been tested using the resources in the 'src/test/java/epics' test 
directory. The main focus of the tests was to discover how the library works so that it can be used optimally for
Wica. Each test attempts to answer one or more questions which I had about the library before I wrote the test.

The tests rely on the network availability of a running softIOC. (See the 'epics_tests.db' in the 'src/test/resources' 
directory). The IOC can be started like this: 
```
   softIoc -d epics_tests.db
```

The softIoc was run using an EPICS base distribution from both R3.14.12.7 and R3.15.5. So far I did not detect any
significant differences in the results.  

Two test scenarios were used:
1. Test Series 1 (EPICS SoftIoc and CA library test process were co-located on the SAME physical machine)
1. Test Series 2 (EPICS SoftIoc and CA library test process were located on DIFFERENT physical machines)


During this work to find out what was happening "on the wire" at times I made use of Michael Davidsaver's ca.lua plugin 
for Wireshark available [here](https://github.com/mdavidsaver/cashark).

I also used the [jvisualvm](https://visualvm.github.io/) tool to gain some insight into the threads that are started by 
the application.

# Main "Take Home" Points

Caveat Emptor: maybe there are/were bugs in the test software which would completely invalidate some of the points below !!

1. Although useful information is given in the ca library README file there is currently no Javadoc for the methods 
   offered by the ca library. This means there is no real contract of behaviour and the developer must guess the costs 
   of the various operations (or make tests like I did). For example it would be useful to know which operations result 
   in a network round-trip and which do not ? Also what thread-safety guarantees does the library offer ? And what 
   threads will be used when performing notifications via the asynchronous method interfaces ? Since nothing is really 
   tied down the performance and behaviour of the implementation is free to vary hugely from one release to another. 
   In my opinion this makes it difficult for users of the library to feel they are operating from a stable base.

1. Creating contexts is relatively expensive and the performance drops off quite quickly as the number of contexts 
   increases (from < 10ms/context to over 100ms/context). With the resources on my machine there was was limit of just 
   a few hundred (more precisely 627).

1. It is quick and easy to create a huge number of channels (eg a million in ~2 seconds).

1. Synchronously connecting channels seems to be "quite slow". With the resources on my machine the connection time 
   was around 10ms/channel. So to connect eg 10,000 channels would around 100 seconds. For bulk operations you
   probably wouldn't want to do that.

1. Asynchronous channel connection is MUCH (eg 100x) faster. Connecting 10,000 channels takes less than a second.

1. Once a channels is connected the synchronous GET performance was faster than I initially expected. One can acquire 
   the value from 10,000 channels in less than a second. Using wireshark I verified that the GET call really does 
   result in a network round trip rather than returning some cached value. (Since there is no javadoc ie documented
   contract for the behaviour offered by the method - the developer currently has to guess).

1. Performing an asynchronous GET on multiple channels is maybe 5-10 times faster than the synchronous GET.

1. There seems to be a **scalability problem with the addValueMonitor** feature. Each monitor seems to result in the 
   creation of a separate Thread and after a few thousand my machine ran out of resources. (I read somewhere that
   a typical stack size for a thread is ~1MB).  I never managed to create monitors on more than about 4000 channels.
   A possible improvement might be to **provide a method signature which allows the library user to specify the 
   Executor** to be used for the callback.

1. There seem to be **some stability issues** with the library. Sometimes it emits WARNING messages which hint that the
   underlying library is under stress. By putting busy-wait loops into the tests I was able to wokround these problems
   and still get good performance. Below is an example of a message that is sometimes emitted:   
```   
Jun 01, 2018 12:35:40 AM org.epics.ca.impl.ResponseHandlers handleResponse
WARNING: Invalid response message (command = 13876) received from: /192.168.0.25:5064
Jun 01, 2018 12:35:40 AM org.epics.ca.impl.ResponseHandlers handleResponse
WARNING: Invalid response message (command = 12544) received from: /192.168.0.25:5064
Jun 01, 2018 12:35:40 AM org.epics.ca.impl.ResponseHandlers handleResponse
```

# Results: Test Series 1 
(EPICS source and CA library test process are located on the same physical machines: Simon's Macbook Pro)

### CA Context Tests
Q1: Can the context manual close feature be relied on to cleanup the created channels ? Answer: **YES**

Q2: Can the context autoclose feature be relied on to cleanup the created channels ? Answer: **YES**

Q3: How many contexts can be created ? Answer: **at least 627**

Q4: What is the context creation cost ? Answer: **See below.**
```
- creating 1 contexts took 10 ms. Average: 10.000 ms
- creating 10 contexts took 372 ms. Average: 37.200 ms
- creating 50 contexts took 3386 ms. Average: 67.720 ms
- creating 100 contexts took 9815 ms. Average: 98.150 ms
- creating 150 contexts took 16673 ms. Average: 111.153 ms
- creating 200 contexts took 24409 ms. Average: 122.045 ms
- creating 250 contexts took 37013 ms. Average: 148.052 ms
- creating 300 contexts took 40950 ms. Average: 136.500 ms
- creating 350 contexts took 49077 ms. Average: 140.220 ms
- creating 400 contexts took 56640 ms. Average: 141.600 ms
- creating 450 contexts took 63996 ms. Average: 142.213 ms
- creating 500 contexts took 71875 ms. Average: 143.750 ms
- creating 550 contexts took 79128 ms. Average: 143.869 ms
- creating 600 contexts took 90011 ms. Average: 150.018 ms
```
Q5: Do all contexts share the same returned object ? Answer: **NO**.

### CA Channel Tests

Q10: How many channels can be created ? Answer: **at least 1000000**

Q11: What is the channel creation cost ? Answer: **See below.**
```
- Creating 1 channels took 0 ms. Average: 0.000 ms
- Creating 10 channels took 14 ms. Average: 1.400 ms
- Creating 100 channels took 18 ms. Average: 0.180 ms
- Creating 1000 channels took 38 ms. Average: 0.038 ms
- Creating 10000 channels took 107 ms. Average: 0.011 ms
- Creating 50000 channels took 344 ms. Average: 0.007 ms
- Creating 100000 channels took 521 ms. Average: 0.005 ms
- Creating 500000 channels took 1496 ms. Average: 0.003 ms
- Creating 1000000 channels took 2322 ms. Average: 0.002 ms
```

Q12: Do all channels connected to the same PV share the same returned object ? Answer: **NO**.

Q13: How many connected channels can the library simultaneously support ? Answer: **at least 20000**

Q14: What is the cost of synchronously connecting channels (using Channel class) ? Answer: **See below.**
```
- Synchronously connecting 1 channels took 5 ms. Average: 5.000 ms
- Synchronously connecting 10 channels took 44 ms. Average: 4.400 ms
- Synchronously connecting 100 channels took 882 ms. Average: 8.820 ms
- Synchronously connecting 500 channels took 4431 ms. Average: 8.862 ms
- Synchronously connecting 1000 channels took 9123 ms. Average: 9.123 ms
- Synchronously connecting 2000 channels took 18319 ms. Average: 9.160 ms
- Synchronously connecting 5000 channels took 44510 ms. Average: 8.902 ms
- Synchronously connecting 10000 channels took 87830 ms. Average: 8.783 ms
- Synchronously connecting 15000 channels took 130885 ms. Average: 8.726 ms
- Synchronously connecting 20000 channels took 173732 ms. Average: 8.687 ms
```

Q15: What is the cost of creating channels which will asynchronously connect ? Answer: **See below.**
```
- Creating 1 channels with asynchronous connect policy took 1 ms. Average: 1.000000 ms
- Creating 10 channels with asynchronous connect policy took 1 ms. Average: 0.100000 ms
- Creating 100 channels with asynchronous connect policy took 2 ms. Average: 0.020000 ms
- Creating 1000 channels with asynchronous connect policy took 6 ms. Average: 0.006000 ms
- Creating 10000 channels with asynchronous connect policy took 117 ms. Average: 0.011700 ms
- Creating 50000 channels with asynchronous connect policy took 227 ms. Average: 0.004540 ms
- Creating 100000 channels with asynchronous connect policy took 295 ms. Average: 0.002950 ms
- Creating 150000 channels with asynchronous connect policy took 549 ms. Average: 0.003660 ms
- Creating 200000 channels with asynchronous connect policy took 634 ms. Average: 0.003170 ms
```

Q16: How long does it take for channels to connect asynchronously ? Answer: **See below.**
```
- Connecting 1 channels asynchronously took 113 ms. Average: 113.000 ms.
- Connecting 10 channels asynchronously took 127 ms. Average: 12.700 ms.
- Connecting 100 channels asynchronously took 128 ms. Average: 1.280 ms.
- Connecting 1000 channels asynchronously took 192 ms. Average: 0.192 ms.
- Connecting 10000 channels asynchronously took 1065 ms. Average: 0.106 ms.
- Connecting 50000 channels asynchronously took 4392 ms. Average: 0.088 ms.
- Connecting 100000 channels asynchronously took 8306 ms. Average: 0.083 ms.
- Connecting 150000 channels asynchronously took 12378 ms. Average: 0.083 ms.
- Connecting 200000 channels asynchronously took 16871 ms. Average: 0.084 ms.
```

Q17: What is the cost of performing a synchronous get on multiple channels (same PV) ? Answer: **See below.**
```
- Synchronous Get from 1 channels took 3 ms. Average: 3.000000 ms
- Synchronous Get from 10 channels took 7 ms. Average: 0.700000 ms
- Synchronous Get from 100 channels took 20 ms. Average: 0.200000 ms
- Synchronous Get from 1000 channels took 72 ms. Average: 0.072000 ms
- Synchronous Get from 10000 channels took 616 ms. Average: 0.061600 ms
- Synchronous Get from 20000 channels took 1128 ms. Average: 0.056400 ms
- Synchronous Get from 40000 channels took 2098 ms. Average: 0.052450 ms
- Synchronous Get from 60000 channels took 3058 ms. Average: 0.050967 ms
- Synchronous Get from 80000 channels took 4220 ms. Average: 0.052750 ms
```

Q18: What is the cost of performing an asynchronous get on multiple channels (same PV) ? Answer: **See below.**
```
- Asynchronous Get from 1 channels took 2 ms. Average: 2.000000 ms
- Asynchronous Get from 10 channels took 4 ms. Average: 0.400000 ms
- Asynchronous Get from 100 channels took 7 ms. Average: 0.070000 ms
- Asynchronous Get from 1000 channels took 54 ms. Average: 0.054000 ms
- Asynchronous Get from 10000 channels took 191 ms. Average: 0.019100 ms
- Asynchronous Get from 20000 channels took 361 ms. Average: 0.018050 ms
- Asynchronous Get from 40000 channels took 861 ms. Average: 0.021525 ms
- Asynchronous Get from 60000 channels took 1228 ms. Average: 0.020467 ms
- Asynchronous Get from 80000 channels took 1574 ms. Average: 0.019675 ms
- Asynchronous Get from 100000 channels took 1914 ms. Average: 0.019140 ms
```

Q19: What is the cost of performing an asynchronous get on multiple channels (different PVs) ? Answer: **See below.**
```
- Asynchronous Get from 1 channels took 1 ms. Average: 1.000000 ms
- Asynchronous Get from 10 channels took 2 ms. Average: 0.200000 ms
- Asynchronous Get from 100 channels took 12 ms. Average: 0.120000 ms
- Asynchronous Get from 1000 channels took 30 ms. Average: 0.030000 ms
- Asynchronous Get from 10000 channels took 177 ms. Average: 0.017700 ms
- Asynchronous Get from 20000 channels took 336 ms. Average: 0.016800 ms
- Asynchronous Get from 40000 channels took 657 ms. Average: 0.016425 ms
- Asynchronous Get from 60000 channels took 977 ms. Average: 0.016283 ms
- Asynchronous Get from 80000 channels took 1300 ms. Average: 0.016250 ms
- Asynchronous Get from 100000 channels took 1619 ms. Average: 0.016190 ms
```

Q20: What is the cost of performing a monitor on multiple channels ? Answer: **See below.**
```
- Asynchronous Monitor from 1 channels took 19 ms. Average: 19.000000 ms
- Asynchronous Monitor from 10 channels took 21 ms. Average: 2.100000 ms
- Asynchronous Monitor from 100 channels took 45 ms. Average: 0.450000 ms
- Asynchronous Monitor from 1000 channels took 213 ms. Average: 0.213000 ms
- Asynchronous Monitor from 2000 channels took 308 ms. Average: 0.154000 ms
- Asynchronous Monitor from 4000 channels took 470 ms. Average: 0.117500 ms
```
**Note:** to get this test to work without network overflow the client code had to **insert a busy-wait loop of 10us !**

**Note:** each monitor seems to create its own thread. So far it has been impossible to create more than ~4000 !**

Q21: What is the cost/performance when using CA to transfer large arrays ? Answer: **See below.**
```
- Transfer time for integer array of 10000 elements took 26 ms. Transfer rate: 1 MB/s
- Transfer time for integer array of 20000 elements took 0 ms. Transfer rate: Infinity MB/s
- Transfer time for integer array of 50000 elements took 0 ms. Transfer rate: Infinity MB/s
- Transfer time for integer array of 100000 elements took 2 ms. Transfer rate: 191 MB/s
- Transfer time for integer array of 200000 elements took 2 ms. Transfer rate: 381 MB/s
- Transfer time for integer array of 500000 elements took 3 ms. Transfer rate: 636 MB/s
- Transfer time for integer array of 1000000 elements took 7 ms. Transfer rate: 545 MB/s
- Transfer time for integer array of 2000000 elements took 12 ms. Transfer rate: 636 MB/s
- Transfer time for integer array of 5000000 elements took 39 ms. Transfer rate: 489 MB/s
- Transfer time for integer array of 10000000 elements took 119 ms. Transfer rate: 321 MB/s
```
**Note:** to get this test to work without network overflow the client code had to **insert a busy-wait loop of 10us !**

### CA Channels Tests

Q31: What is the cost of synchronously connecting channels (using Channels class) ? Answer: **See below.**
```
- Synchronously connecting 1 channels took 21 ms. Average: 21.000 ms
- Synchronously connecting 10 channels took 187 ms. Average: 18.700 ms
- Synchronously connecting 100 channels took 1037 ms. Average: 10.370 ms
- Synchronously connecting 500 channels took 4736 ms. Average: 9.472 ms
- Synchronously connecting 1000 channels took 9201 ms. Average: 9.201 ms
- Synchronously connecting 2000 channels took 17895 ms. Average: 8.948 ms
- Synchronously connecting 5000 channels took 44073 ms. Average: 8.815 ms
- Synchronously connecting 10000 channels took 86118 ms. Average: 8.612 ms
- Synchronously connecting 15000 channels took 128131 ms. Average: 8.542 ms
```

# Results: Test Series 2 
(EPICS source and CA library test process are located on DIFFERENT physical machines)

### CA Context Tests

Q1: Can the context manual close feature be relied on to cleanup the created channels ? Answer: **YES**

Q2: Can the context autoclose feature be relied on to cleanup the created channels ? Answer: **YES**

Q3: How many contexts can be created ? Answer: **at least 639**.

Q4: What is the context creation cost ? Answer: **See below.**
```
- creating 1 contexts took 16 ms. Average: 16.000 ms
- creating 10 contexts took 409 ms. Average: 40.900 ms
- creating 50 contexts took 4245 ms. Average: 84.900 ms
- creating 100 contexts took 10457 ms. Average: 104.570 ms
- creating 150 contexts took 18674 ms. Average: 124.493 ms
- creating 200 contexts took 26923 ms. Average: 134.615 ms
- creating 250 contexts took 32972 ms. Average: 131.888 ms
- creating 300 contexts took 42159 ms. Average: 140.530 ms
- creating 350 contexts took 49570 ms. Average: 141.629 ms
- creating 400 contexts took 56994 ms. Average: 142.485 ms
- creating 450 contexts took 62866 ms. Average: 139.702 ms
- creating 500 contexts took 70503 ms. Average: 141.006 ms
- creating 550 contexts took 77944 ms. Average: 141.716 ms
- creating 600 contexts took 85877 ms. Average: 143.128 ms
```
Q5: Do all contexts share the same returned object ? Answer: **NO**.

### CA Channel Tests

Q10: How many channels can be created ? Answer: **at least 1,000,000**

Q11: What is the channel creation cost ? Answer: **See below.**
```
- Creating 1 channels took 0 ms. Average: 0.000 ms
- Creating 10 channels took 6 ms. Average: 0.600 ms
- Creating 100 channels took 7 ms. Average: 0.070 ms
- Creating 1000 channels took 46 ms. Average: 0.046 ms
- Creating 10000 channels took 153 ms. Average: 0.015 ms
- Creating 50000 channels took 307 ms. Average: 0.006 ms
- Creating 100000 channels took 501 ms. Average: 0.005 ms
- Creating 500000 channels took 1491 ms. Average: 0.003 ms
- Creating 1000000 channels took 2603 ms. Average: 0.003 ms
```
Q12: Do all channels connected to the same PV share the same returned object ? Answer: **NO**.

Q13: How many connected channels can the library simultaneously support ? Answer: **at least 20,000**

Q14: What is the cost of synchronously connecting channels (using Channel class) ? Answer: **See below.**
```
- Synchronously connecting 1 channels took 5 ms. Average: 5.000 ms
- Synchronously connecting 10 channels took 52 ms. Average: 5.200 ms
- Synchronously connecting 100 channels took 920 ms. Average: 9.200 ms
- Synchronously connecting 500 channels took 4802 ms. Average: 9.604 ms
- Synchronously connecting 1000 channels took 9651 ms. Average: 9.651 ms
- Synchronously connecting 2000 channels took 19954 ms. Average: 9.977 ms
- Synchronously connecting 5000 channels took 48301 ms. Average: 9.660 ms
- Synchronously connecting 10000 channels took 97317 ms. Average: 9.732 ms
- Synchronously connecting 15000 channels took 148564 ms. Average: 9.904 ms
- Synchronously connecting 20000 channels took 199485 ms. Average: 9.974 ms
```

Q15: What is the cost of creating channels which will asynchronously connect ? Answer: **See below.**
```
- Creating 1 channels with asynchronous connect policy took 9 ms. Average: 9.000000 ms
- Creating 10 channels with asynchronous connect policy took 9 ms. Average: 0.900000 ms
- Creating 100 channels with asynchronous connect policy took 10 ms. Average: 0.100000 ms
- Creating 1000 channels with asynchronous connect policy took 39 ms. Average: 0.039000 ms
- Creating 10000 channels with asynchronous connect policy took 133 ms. Average: 0.013300 ms
- Creating 50000 channels with asynchronous connect policy took 504 ms. Average: 0.010080 ms
- Creating 100000 channels with asynchronous connect policy took 621 ms. Average: 0.006210 ms
- Creating 150000 channels with asynchronous connect policy took 766 ms. Average: 0.005107 ms
- Creating 200000 channels with asynchronous connect policy took 1123 ms. Average: 0.005615 ms
```

Q16: How long does it take for channels to connect asynchronously ? Answer: **See below.**
```
- Connecting 1 channels asynchronously took 15 ms. Average: 15.000 ms.
- Connecting 10 channels asynchronously took 19 ms. Average: 1.900 ms.
- Connecting 100 channels asynchronously took 20 ms. Average: 0.200 ms.
- Connecting 1000 channels asynchronously took 105 ms. Average: 0.105 ms.
- Connecting 10000 channels asynchronously took 1184 ms. Average: 0.118 ms.
- Connecting 50000 channels asynchronously took 4547 ms. Average: 0.091 ms.
- Connecting 100000 channels asynchronously took 8697 ms. Average: 0.087 ms.
- Connecting 150000 channels asynchronously took 12782 ms. Average: 0.085 ms.
- Connecting 200000 channels asynchronously took 17288 ms. Average: 0.086 ms.
```

Q17: What is the cost of performing a synchronous get on multiple channels (same PV) ? Answer: **See below.**
```
- Synchronous Get from 1 channels took 3 ms. Average: 3.000000 ms
- Synchronous Get from 10 channels took 10 ms. Average: 1.000000 ms
- Synchronous Get from 100 channels took 49 ms. Average: 0.490000 ms
- Synchronous Get from 1000 channels took 300 ms. Average: 0.300000 ms
- Synchronous Get from 10000 channels took 2616 ms. Average: 0.261600 ms
- Synchronous Get from 20000 channels took 5358 ms. Average: 0.267900 ms
- Synchronous Get from 40000 channels took 10866 ms. Average: 0.271650 ms
- Synchronous Get from 60000 channels took 16353 ms. Average: 0.272550 ms
- Synchronous Get from 80000 channels took 21839 ms. Average: 0.272988 ms
```

Q18: What is the cost of performing an asynchronous get on multiple channels (same PV) ? Answer: **See below.**
```
- Asynchronous Get from 1 channels took 4 ms. Average: 4.000000 ms
- Asynchronous Get from 10 channels took 6 ms. Average: 0.600000 ms
- Asynchronous Get from 100 channels took 13 ms. Average: 0.130000 ms
- Asynchronous Get from 1000 channels took 61 ms. Average: 0.061000 ms
- Asynchronous Get from 10000 channels took 300 ms. Average: 0.030000 ms
- Asynchronous Get from 20000 channels took 600 ms. Average: 0.030000 ms
- Asynchronous Get from 40000 channels took 1137 ms. Average: 0.028425 ms
- Asynchronous Get from 60000 channels took 1686 ms. Average: 0.028100 ms
- Asynchronous Get from 80000 channels took 2489 ms. Average: 0.031112 ms
- Asynchronous Get from 100000 channels took 3027 ms. Average: 0.030270 ms
```
**Note:** to get this test to work without network overflow the client code had to **insert a busy-wait loop of 10us !** 

Q19: What is the cost of performing an asynchronous get on multiple channels (different PVs) ? Answer: **See below.**
```
- Asynchronous Get from 1 channels took 1 ms. Average: 1.000000 ms
- Asynchronous Get from 10 channels took 3 ms. Average: 0.300000 ms
- Asynchronous Get from 100 channels took 10 ms. Average: 0.100000 ms
- Asynchronous Get from 1000 channels took 44 ms. Average: 0.044000 ms
- Asynchronous Get from 10000 channels took 282 ms. Average: 0.028200 ms
- Asynchronous Get from 20000 channels took 553 ms. Average: 0.027650 ms
- Asynchronous Get from 40000 channels took 1265 ms. Average: 0.031625 ms
- Asynchronous Get from 60000 channels took 1832 ms. Average: 0.030533 ms
- Asynchronous Get from 80000 channels took 2366 ms. Average: 0.029575 ms
- Asynchronous Get from 100000 channels took 2904 ms. Average: 0.029040 ms
```
**Note:** to get this test to work without network overflow the client code had to **insert a busy-wait loop of 10us !** 


Q20: What is the cost of performing a monitor on multiple channels ? Answer: **See below.**
```
- Asynchronous Monitor from 1 channels took 23 ms. Average: 23.000000 ms
- Asynchronous Monitor from 10 channels took 25 ms. Average: 2.500000 ms
- Asynchronous Monitor from 100 channels took 55 ms. Average: 0.550000 ms
- Asynchronous Monitor from 1000 channels took 196 ms. Average: 0.196000 ms
- Asynchronous Monitor from 2000 channels took 296 ms. Average: 0.148000 ms
- Asynchronous Monitor from 4000 channels took 407 ms. Average: 0.101750 ms
```
**Note:** to get this test to work without network overflow the client code had to **insert a busy-wait loop of 10us !**

**Note:** each monitor seems to create its own thread. So far it has been **impossible to create more than ~4000 !**

Q21: What is the cost/performance when using CA to transfer large arrays ? Answer: **See below.**
```
- Transfer time for integer array of 10000 elements took 4 ms. Transfer rate: 10 MB/s
- Transfer time for integer array of 20000 elements took 4 ms. Transfer rate: 19 MB/s
- Transfer time for integer array of 50000 elements took 4 ms. Transfer rate: 48 MB/s
- Transfer time for integer array of 100000 elements took 5 ms. Transfer rate: 76 MB/s
- Transfer time for integer array of 200000 elements took 9 ms. Transfer rate: 85 MB/s
- Transfer time for integer array of 500000 elements took 22 ms. Transfer rate: 87 MB/s
- Transfer time for integer array of 1000000 elements took 43 ms. Transfer rate: 89 MB/s
- Transfer time for integer array of 2000000 elements took 88 ms. Transfer rate: 87 MB/s
- Transfer time for integer array of 5000000 elements took 229 ms. Transfer rate: 83 MB/s
- Transfer time for integer array of 10000000 elements took 425 ms. Transfer rate: 90 MB/s
```

### CA Channels Tests

Q31: What is the cost of synchronously connecting channels (using Channels class) ? Answer: **See below.**
```
- Synchronously connecting 1 channels took 12 ms. Average: 12.000 ms
- Synchronously connecting 10 channels took 64 ms. Average: 6.400 ms
- Synchronously connecting 100 channels took 1040 ms. Average: 10.400 ms
- Synchronously connecting 500 channels took 5232 ms. Average: 10.464 ms
- Synchronously connecting 1000 channels took 10359 ms. Average: 10.359 ms
- Synchronously connecting 2000 channels took 20546 ms. Average: 10.273 ms
- Synchronously connecting 5000 channels took 51339 ms. Average: 10.268 ms
- Synchronously connecting 10000 channels took 100855 ms. Average: 10.085 ms
- Synchronously connecting 15000 channels took 151024 ms. Average: 10.068 ms
```