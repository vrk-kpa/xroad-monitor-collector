# Collector for X-Road monitoring data

## Build

To compile, run unit tests and integration tests

    $ ./gradlew clean build


## Run

To run the application

    $ ./gradlew bootRun

Or

    $ java -jar build/libs/xroad-monitor-collector.jar

Then run the collector with profile production

    $ java -Dspring.profiles.active=production -jar build/libs/xroad-monitor-collector.jar --spring.config.name=application


## Running tests

To run unit tests

    $ ./gradlew clean test

To run integration tests

    $ ./gradlew clean integrationTest

To and run all tests

    $ ./gradlew clean test integrationTest

Integration tests require that you are able to connect to the specified security server and Elasticsearch instance.


## Build RPM Packages on Non-RedHat Platform
 
    $ ./gradlew clean build
    $ docker build -t collector-rpm packages/xroad-monitor-collector/docker
    $ docker run -v $PWD/..:/workspace  -v /etc/passwd:/etc/passwd:ro -v /etc/group:/etc/group:ro collector-rpm


## Format license header format

This will add license header to all *.java files.

    $ ./gradlew licenseFormat
    
## Using getSecurityServerMetricDataRequest parameters
Using metricdatarequest parameters happens by altering application.properties and for production application-production.properties file. In properties file next line should be edited:
```
   xroad-monitor-collector.query-parameters=
```
Line what is shown up will request all metricdata, if only some of metric data is wanted. To that line should be added names of metric data like here:
```
   xroad-monitor-collector.query-parameters=OperatingSystem,Processes
```
Names should be seperated with ',' and there should not be any spaces.
You can find the different monitoring data metric names from the document: [X-Road EnvironmentalMonitoring](https://github.com/vrk-kpa/X-Road/tree/develop/doc/EnvironmentalMonitoring)

## SSL

To enable secure HTTPS connection to security server with mutual authentication follow the steps below.

Create new keystore and keypair for xroad-monitor-collector
`keytool -alias xroad-monitor-collector -genkeypair -keystore /etc/xroad/xroad-monitor-collector/keystore -validity 7300 -keyalg RSA -keysize 2048 -sigalg SHA256withRSA -dname C=FI,CN=xroad-monitor-collector`

Export the xroad-monitor-collector certificate to file
`keytool -keystore /etc/xroad/xroad-monitor-collector/keystore -exportcert -rfc -alias xroad-monitor-collector > xroad-monitor-collector.cer`

Configure the security server to use HTTPS connection and import the xroad-monitor-collector certificate to internal servers - internal TLS certificates list.

Download the security server's certificate
`openssl s_client -showcerts -connect myserver.example.com:443  </dev/null`

From the output extract the PEM certificate and save it to file myserver.cer

Create new truststore for xroad-monitor-collector
`keytool -import -file myserver.cer -alias myserver -keystore truststore`

Configure the xroad-monitor-collector application to use the created keystore and truststore in the properties file
```
xroad-monitor-collector-client.ssl-keystore=/etc/xroad/xroad-monitor-collector/keystore
xroad-monitor-collector-client.ssl-keystore-password=secret
xroad-monitor-collector-client.ssl-truststore=/etc/xroad/xroad-monitor-collector/truststore
xroad-monitor-collector-client.ssl-truststore-password=secret
```
