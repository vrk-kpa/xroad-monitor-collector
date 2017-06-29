# Collector for X-Road monitoring data

## Build


    $ ./gradlew clean build


## Run

    $ ./gradlew bootRun

Or

    $ java -jar build/libs/xroad-monitor-collector.jar

Then run the collector with profile production

    $ java -Dspring.profiles.active=production -jar build/libs/xroad-monitor-collector.jar --spring.config.name=collector


## Build RPM Packages on Non-RedHat Platform
 
    $ ./gradlew clean build
    $ docker build -t collector-rpm packages/xroad-monitor-collector/docker
    $ docker run -v $PWD/..:/workspace  -v /etc/passwd:/etc/passwd:ro -v /etc/group:/etc/group:ro collector-rpm

## Format license header format

This will add license header to all *.java files.

    $ ./gradlew licenseFormat