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
Requires:           systemd, java-1.8.0-openjdk, xroad-common
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

%clean
rm -rf %{buildroot}

%files
%attr(644,root,root) %{_unitdir}/%{name}.service
%attr(644,root,root) /etc/cron.d/xroad-monitor-collector.cron
%attr(744,xroad-monitor-collector,xroad-monitor-collector) %{jlib}/%{name}.jar
%attr(744,xroad-monitor-collector,xroad-monitor-collector) /usr/share/xroad/bin/%{name}

%pre

if ! id xroad-monitor-collector > /dev/null 2>&1 ; then
    adduser --system --no-create-home --shell /bin/false xroad-monitor-collector
fi

%post

%systemd_post %{name}.service

%preun
%systemd_preun %{name}.service

%postun
%systemd_postun_with_restart %{name}.service


%changelog
