# do not repack jars
%define __jar_repack %{nil}
# produce .elX dist tag on both centos and redhat
%define dist %(/usr/lib/rpm/redhat/dist.sh)

Name:               xroad-monitor-collector
Version:            %{xroad_monitor_collector_version}
Release:            %{rel}%{?snapshot}%{?dist}
Summary:            X-Road Monitoring Data Collector
Group:              Applications/Internet
License:            MIT
Requires:           systemd, java-1.8.0-openjdk, xroad-confclient
Requires(post):     systemd
Requires(preun):    systemd
Requires(postun):   systemd

%define src %{_topdir}
%define jlib /usr/lib/xroad-monitor-collector
%define conf /etc/xroad/xroad-monitor-collector

%description
X-Road monitoring data collector

%prep

%build

%install
mkdir -p %{buildroot}%{jlib}
mkdir -p %{buildroot}%{conf}
mkdir -p %{buildroot}%{_unitdir}
mkdir -p %{buildroot}/usr/share/xroad/bin
mkdir -p %{buildroot}/var/log/xroad
mkdir -p %{buildroot}/etc/cron.d
cp -p %{src}/../../../build/libs/xroad-monitor-collector.jar %{buildroot}%{jlib}
cp -p %{src}/SOURCES/%{name} %{buildroot}/usr/share/xroad/bin
cp -p %{src}/SOURCES/%{name}.service %{buildroot}%{_unitdir}
cp -p %{src}/SOURCES/%{name}.cron %{buildroot}/etc/cron.d
cp -p %{src}/../../../src/main/resources/application.properties %{buildroot}/etc/xroad/xroad-monitor-collector

%clean
rm -rf %{buildroot}

%files
%attr(644,root,root) %{_unitdir}/%{name}.service
%attr(644,root,root) /etc/cron.d/xroad-monitor-collector.cron
%attr(744,xroad,xroad) %{jlib}/%{name}.jar
%attr(744,xroad,xroad) %config /usr/share/xroad/bin/%{name}
%attr(644,xroad,xroad) %config /etc/xroad/xroad-monitor-collector/application.properties

%pre

%post

%systemd_post %{name}.service

%preun
%systemd_preun %{name}.service

%postun
%systemd_postun_with_restart %{name}.service


%changelog
