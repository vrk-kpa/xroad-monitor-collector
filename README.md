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


## Build RPM Packages

The packaging uses Docker and the packages can also be built on non-RedHat platform.

To build development packages
```
$ ./gradlew clean build
$ ./build_packages.sh
```

To build release packages
```
$ ./gradlew clean build
$ ./build_release_packages.sh
```

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

To enable secure HTTPS connection to central monitoring client security server with mutual authentication follow the steps below.

Create new keystore and keypair for xroad-monitor-collector
```
keytool -alias xroad-monitor-collector -genkeypair -keystore /etc/xroad/xroad-monitor-collector/keystore -validity 7300 -keyalg RSA -keysize 2048 -sigalg SHA256withRSA -dname C=FI,CN=xroad-monitor-collector
```

Export the xroad-monitor-collector certificate to file
```
keytool -keystore /etc/xroad/xroad-monitor-collector/keystore -exportcert -rfc -alias xroad-monitor-collector > xroad-monitor-collector.cer
```

Using the security server administrator interface configure the security server to use HTTPS connection and import the xroad-monitor-collector certificate from previous step to "Internal servers "- "Internal TLS certificates" list.

From the security server's administrator user interface export the security server internal certificate "System Parameters" - "Internal TLS Certificate" - "Export" and save it to file e.g. myserver.cer

Create new truststore for xroad-monitor-collector and import the trusted certificate
```
keytool -import -file myserver.cer -alias myserver -keystore /etc/xroad/xroad-monitor-collector/truststore
```

By default the xroad-monitor-collector uses the following paths and passwords for the keystore and truststore
```
xroad-monitor-collector-client.ssl-keystore=/etc/xroad/xroad-monitor-collector/keystore
xroad-monitor-collector-client.ssl-keystore-password=secret
xroad-monitor-collector-client.ssl-truststore=/etc/xroad/xroad-monitor-collector/truststore
xroad-monitor-collector-client.ssl-truststore-password=secret
```

Should you need to modify the default paths or passwords please refer to [Spring Boot external config documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html)
