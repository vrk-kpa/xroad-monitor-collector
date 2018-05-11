# Collector for X-Road monitoring data

## General

X-Road Monitor Collector is a software that collects monitoring data from X-Road instance and stores it in [Elasticsearch](https://www.elastic.co/products/elasticsearch) for later analysis.


## Build

To compile, run unit tests and integration tests (note that to run the integration tests you need to have Elasticsearch installed and X-Road Monitor Collector configured to use it)

    $ ./gradlew clean build
    
To compile and run unit tests and no integration tests

    $ ./gradlew clean build -x integrationTest

## Run

To run the application

    $ ./gradlew bootRun

Or

    $ java -jar build/libs/xroad-monitor-collector.jar
    
Run collector with external config (looks for application.properties from /etc/xroad/xroad-monitor-collector/)

    $ java -jar build/libs/xroad-monitor-collector.jar --spring.config.location=/etc/xroad/xroad-monitor-collector/


## Running tests

To run unit tests

    $ ./gradlew clean test

To run integration tests

    $ ./gradlew clean integrationTest
    
To and run all tests

    $ ./gradlew clean test integrationTest

Integration tests require that you are able to connect to the specified security server and Elasticsearch instance.


## Format license header format

This will add license header to all *.java files.

    $ ./gradlew licenseFormat

## Build RPM Packages

The packaging uses Docker and the packages can also be built on non-RedHat platform.

To build development packages

    $ ./gradlew clean build
    $ ./build_packages.sh

To build release packages

    $ ./gradlew clean build
    $ ./build_release_packages.sh

## Installation

The xroad-monitor-collector package built in previous step depends on java-1.8.0-openjdk and xroad-confclient.

To install OpenJDK

    $ sudo yum install -y java-1.8.0-openjdk


To make xroad-confclient available you have two options. 
1. Configure a public repository and install it from there, see [instructions](https://esuomi.fi/palveluntarjoajille/palveluvayla/ymparistot/asennusohjeet/palveluvaylaohjelmiston-asennusohjeet-liityntapalvelimille-rhel7/)
2. Build X-Road yourself as described [here](https://github.com/ria-ee/X-Road/blob/develop/src/BUILD.md) and install it with `rpm` command

Finally install X-Road Monitor Collector with

    $ rpm -i xroad-monitor-collector-1.1.0-0.20171122143212gitb2f4a55.el7.x86_64

Replace the filename with your package name accordingly.

## Configuration

After the xroad-monitor-collector package and its dependencies have been installed the software needs to configured. The xroad-confclient module fetches global configuration from a central server periodically. It uses a configuration file called configuration anchor which is downloaded from the X-Road instance's central server (global configuration - anchor - download) you wish to monitor. The configuration anchor is placed to `/etc/xroad/configuration-anchor.xml`.

X-Road Monitor Collector is configured with `/etc/xroad/xroad-monitor-collector/application.properties`. The file is installed by the package and comes with default settings that need to be adapted to the running environment.

## Using getSecurityServerMetricDataRequest parameters

Using metricdatarequest parameters happens by altering application.properties and for production application-production.properties file. In properties file next line should be edited:

    $ xroad-monitor-collector.query-parameters=

Line what is shown up will request all metricdata, if only some of metric data is wanted. To that line should be added names of metric data like here:

    $ xroad-monitor-collector.query-parameters=OperatingSystem,Processes

Names should be seperated with ',' and there should not be any spaces.
You can find the different monitoring data metric names from the document: [X-Road EnvironmentalMonitoring](https://github.com/vrk-kpa/X-Road/tree/develop/doc/EnvironmentalMonitoring)

## SSL

To enable secure HTTPS connection to central monitoring client security server with mutual authentication follow the steps below.

Create new keystore and keypair for xroad-monitor-collector

    $ keytool -alias xroad-monitor-collector -genkeypair -keystore /etc/xroad/xroad-monitor-collector/keystore -validity 7300 -keyalg RSA -keysize 2048 -sigalg SHA256withRSA -dname C=FI,CN=xroad-monitor-collector

Export the xroad-monitor-collector certificate to file

    $ keytool -keystore /etc/xroad/xroad-monitor-collector/keystore -exportcert -rfc -alias xroad-monitor-collector > xroad-monitor-collector.cer

Using the security server administrator interface configure the security server to use HTTPS connection and import the xroad-monitor-collector certificate from previous step to "Internal servers "- "Internal TLS certificates" list.

From the security server's administrator user interface export the security server internal certificate "System Parameters" - "Internal TLS Certificate" - "Export" and save it to file e.g. myserver.cer

Create new truststore for xroad-monitor-collector and import the trusted certificate

    $ keytool -import -file myserver.cer -alias myserver -keystore /etc/xroad/xroad-monitor-collector/truststore

By default the xroad-monitor-collector uses the following paths and passwords for the keystore and truststore
	
    $ xroad-monitor-collector-client.ssl-keystore=/etc/xroad/xroad-monitor-collector/keystore
    $ xroad-monitor-collector-client.ssl-keystore-password=secret
    $ xroad-monitor-collector-client.ssl-truststore=/etc/xroad/xroad-monitor-collector/truststore
    $ xroad-monitor-collector-client.ssl-truststore-password=secret

Should you need to modify the default paths or passwords please refer to [Spring Boot external config documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html)
