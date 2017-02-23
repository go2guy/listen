#######################################################################
# controller.spec
#######################################################################
%define STARTDIR %(pwd)
%define _arch noarch

# by default, the /etc/rpm/macros.dist defines this values as .el7.centos.
%if 0%{?rhel} == 7
  %define dist .el7
  # CentOS 7 would force ".el7.centos", we want to avoid that.
%endif

Summary: NewNet Listen Controller
Name: listen-controller
Version: 2.0%{dist}
Release: %(echo "${RELEASE}")
License: Copyright (c) NewNet Communication Technologies. All Rights Reserved.
Group: Interact
URL: http://www.newnet.com
Vendor: NewNet Communication Technologies
Packager: listen <listenpkg@newnet.com>
BuildArch: %{_arch}
Requires: xmlsecurity, iijava, iitomcat, installCoordinator

%define __spec_install_pre %{nil}
%define __spec_install_post /usr/lib/rpm/brp-compress
%define _topdir %(echo "${TOPDIR}")
%define _unpackaged_files_terminate_build 0
%(mkdir -p %{_topdir}/BUILD/)
%(mkdir -p %{_topdir}/RPMS/%{_arch})
%(mkdir -p %{_topdir}/SRPMS/)
%(mkdir -p %{_topdir}/SOURCES/)

#######################################################################
# The description section is used to explain what the package does,
# describe any warnings or additional configuration instructions,
# etc. Anyone using rpm -qi to query your package will be given
# this description.
#######################################################################
%description
    This RPM will install/uninstall the Listen Controller.

#######################################################################
# The prep command section is used to unpack the source code into a
# temporary directory and apply any patches.
#######################################################################
%prep

#######################################################################
# The build section is used to compile the code.
#######################################################################
%build

#######################################################################
# The install section is used to install the code into directories
# on the build machine
#######################################################################
%install

#######################################################################
# The files section lists all files included in the RPM
#######################################################################
%files
    %defattr(775, interact,operator)

    # Include everything in the /interact/listen directory
    %dir /interact/listen/logs
    /interact/listen
    /interact/tomcat/webapps/listen-controller.war
    %config(noreplace) /interact/tomcat/lib/listen-controller.properties
    %config(noreplace) /interact/tomcat/lib/log4j.listen.properties
    %config(noreplace) /interact/collector/conf/listen.stats

#######################################################################
# clean is a script that gets run at the end of the RPM building,
# if everything works, so that your temporary files don't hang
# around forever
#######################################################################
%clean

#######################################################################
# This is a log of what changes occurred when the package is updated.
# If you are modifying an existing RPM it is a good idea to list what
# changes you made here.
#######################################################################
%changelog

#######################################################################
# The pre section lists actions to be performed before installation
#######################################################################
%pre

#######################################################################
# The post section lists actions to be performed after installation
#######################################################################
%post
#ln -sf /interact/listen/scripts/listen-controller /etc/init.d/listen-controller
#chkconfig --add listen-controller

    logfile="/interact/packages/logs/uia.log"
    if [ ! -d `dirname ${logfile}` ]
    then
        mkdir -p `dirname ${logfile}`
        if [ $? -ne 0 ]
        then
            echo "Error creating logs directory [ `dirname ${logfile}` ]."
            logfile=/dev/null
        fi
    fi

    # Remove everything from the /work directory so that the cache's are cleared.
    cd /interact/tomcat/work
    find -type d -name "listen-controller" -exec rm -rf {} \; > /dev/null 2>&1

    # Remove the war directory from webapps.
    for warpkg in `rpm -ql %{name}-%{version}-%{release} | grep "\.war"`
    do
        wardir=`basename "${warpkg}" | sed -e "s@\.war@@g"`
        if [ -d /interact/tomcat/webapps/${wardir}/ ]
        then
            rm -rf /interact/tomcat/webapps/${wardir}/
        fi
    done


#######################################################################
# The preun section lists actions to be performed before
# un-installation
#######################################################################
%preun
#if [ "$1" -le "0" ]; then
#  chkconfig --del listen-controller
#  rm -f /etc/init.d/listen-controller
#  unlink /etc/init.d/listen
#fi

    logfile="/interact/packages/logs/uia.log"
    if [ ! -d `dirname ${logfile}` ]
    then
        mkdir -p `dirname ${logfile}`
        if [ $? -ne 0 ]
        then
            echo "Error creating logs directory [ `dirname ${logfile}` ]."
            logfile=/dev/null
        fi
    fi



#######################################################################
# The postun section lists actions to be performed after
# un-installation
#######################################################################
%postun

