FROM centos:7
MAINTAINER "Ilkka Seppälä" <ilkka.seppala@gofore.com>

RUN yum -y install sudo git rpm-build java-1.8.0-openjdk-headless
RUN yum clean all
RUN sed -i 's/requiretty/!requiretty/' /etc/sudoers

USER root
CMD ["sh", "-c", "/workspace/xroad-monitor-collector/build_rpm.sh" ]
