#!/bin/sh

docker build -t collector-rpm packages/xroad-monitor-collector/docker
docker run -v $PWD/..:/workspace  -v /etc/passwd:/etc/passwd:ro -v /etc/group:/etc/group:ro -u $(id -u):$(id -g) collector-rpm /workspace/xroad-monitor-collector/build_rpm.sh -release
