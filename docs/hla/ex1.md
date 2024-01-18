# Install and Test the Wica-HTTP Server


### Login to our development machine and and enter a fresh directory for our tests

Example (change for your PSI LDAP user id)
```
ssh rees_s@sls20-bd-testsrv.psi.ch
mkdir hla_wica
cd hla_wica
```

### Download the latest Wica-Http release jar file

```
curl -L https://github.com/paulscherrerinstitute/wica-http/releases/download/1.14.0/wica-http-1.14.0.jar -o wica-http.jar
```

### Run the Wica-HTTP Server Jar file on some unique port number

A convenient way to create a unique port number is to use your PSI telephone number. In my case it's ext 5825

Example (change ext 5825 to your PSI telephone number)
```
java -jar wica-http.jar --server.port=5825 --server.address=sls20-bd-testsrv.psi.ch
```

### Using the browser on your laptop navigate to the Wica-Http Admin page at the following URL

Note: since the demo uses HTTP not HTTPS you may need to configure your browser to accept insecure connections.

Example (change the port number to the same one you created earlier)
```
http://sls20-bd-testsrv.psi.ch:5825/admin
```
