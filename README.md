# Collector for X-Road monitoring data

## Build


    $ gradle clean build


## Run

    $ gradle bootRun

Or

    $ java -jar build/libs/xroad-monitor-collector.jar

## Build RPM Packages on Non-RedHat Platform
 
    $ gradle clean build
    $ docker build -t collector-rpm packages/xroad-monitor-collector/docker
    $ docker run -v $PWD/..:/workspace  -v /etc/passwd:/etc/passwd:ro -v /etc/group:/etc/group:ro collector-rpm
