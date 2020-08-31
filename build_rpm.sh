#!/bin/sh

DIR="workspace/xroad-monitor-collector/packages/xroad-monitor-collector/redhat"
cd $DIR
ROOT=`pwd`

if [[ $1 == "-release" ]] ; then
  RELEASE=1
  SNAPSHOT=
else
  RELEASE=0
  DATE=$(date --utc --date @$(git show -s --format=%ct || date +%s) +'%Y%m%d%H%M%S')
  HASH=$(git show -s --format=git%h || echo 'local')
  SNAPSHOT=.$DATE$HASH
fi

CMD="-ba"

rm -rf ${ROOT}/RPMS/*
rm -rf ${ROOT}/SRPMS/*

rpmbuild \
    --define "xroad_monitor_collector_version 1.2.1" \
    --define "rel $RELEASE" \
    --define "snapshot $SNAPSHOT" \
    --define "_topdir $ROOT" \
    -${CMD} SPECS/xroad-monitor-collector.spec
