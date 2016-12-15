#######################################################################
# provisioner.spec
######################################################################
%define STARTDIR %(pwd)
%define _topdir %{STARTDIR}

%ifnarch x86_64
%define _arch i686
%endif

Summary: Listen TFTP Provisioning Server
Name: provisioner
Version: %(if [ -z $JENKINS_VERSION ]; then echo "99.0"; else echo "${JENKINS_VERSION}"; fi)
Release: 1
License: Copyright (c) NewNet Communication Technologies. All Rights Reserved.
Group: Interact
URL: http://www.newnet.com/
Vendor: NewNet Communication Technologies
Packager: iiSupport <iiSupport@newnet.com>
BuildArch: %{_arch}
BuildRoot: %{STARTDIR}/BUILD/%{name}-%{version}-%{release}-buildroot
Requires: iinodejs >= 6.3.1

%(mkdir -p %{_topdir}/RPMS/%{buildarch})
%(mkdir -p %{_topdir}/SRPMS/)
%(mkdir -p %{_topdir}/BUILD/)
%(mkdir -p %{_topdir}/SOURCES/)

%define __spec_install_pre %{nil}
%define __spec_install_post /usr/lib/rpm/brp-compress

#######################################################################
# The description section is used to explain what the package does,
# describe any warnings or additional configuration instructions,
# etc. Anyone using rpm -qi to query your package will see be given
# this description.
#######################################################################
%description
    Provisioner is a TFTP Server used to provision SIP phones

#######################################################################
# The prep command section is used to unpack the source code into a
# temporary directory and apply any patches.
#######################################################################
%prep
    # Clean
    rm -rf %{_topdir}/RPMS/*
    rm -rf %{_topdir}/SRPMS/*
    rm -rf %{_topdir}/BUILD/*

    mkdir -p %{buildroot}/etc/rc.d/init.d/ \
             %{buildroot}/interact/lock/ \
             %{buildroot}/interact/logs/ \
             %{buildroot}/interact/provisioner \
             %{buildroot}/interact/provisioner/scripts/ \
             %{buildroot}/interact/provisioner/root/

#######################################################################
# The build section is used to compile the code.
#######################################################################
%build
    cp %{STARTDIR}/src/package.json %{buildroot}/interact/provisioner
    cd %{buildroot}/interact/provisioner

    npm install --unsafe-perm

#######################################################################
# The install section is used to install the code into directories
# on the build machine
#######################################################################
%install

    cp %{STARTDIR}/scripts/provisioner %{buildroot}/etc/rc.d/init.d/
    touch %{buildroot}/interact/lock/provisioner.pid
    touch %{buildroot}/interact/logs/provisioner.log
    cp -r %{STARTDIR}/src/* %{buildroot}/interact/provisioner/

    # Remove any svn/git directories
    ctrldirs="`find %{buildroot} -type d -name .svn -o -name .git`"
    if [ "${ctrldirs}" != "" ]
    then
        rm -rf ${ctrldirs}
    fi

#######################################################################
# The files section lists all files included in the RPM
#######################################################################
%files
    %defattr(755,interact,operator)
    %attr(755,root,root) /etc/rc.d/init.d/provisioner
    /interact

    %attr(775,interact,operator) %dir /interact/
    %attr(777,interact,operator) %dir /interact/logs/
    %config(noreplace) /interact/provisioner/server/config.js
    %ghost /interact/lock/provisioner.pid
    %ghost /interact/logs/provisioner.log

#######################################################################
# clean is a script that gets run at the end of the RPM building,
# if everything works, so that your temporary files don't hang
# around forever
#######################################################################
%clean
    rm -rf %{STARTDIR}/ii_artifacts/*.rpm
    mkdir -p %{STARTDIR}/ii_artifacts/
    mv -f %{_topdir}/RPMS/%{buildarch}/*.rpm %{STARTDIR}/ii_artifacts/

    if [ "%{buildroot}" != "" ]
    then
        rm -rf %{buildroot}
    fi

    if [ "%{_topdir}" != "" ]
    then
        rm -rf %{_topdir}/RPMS %{_topdir}/SRPMS %{_topdir}/BUILD/ %{_topdir}/SOURCES
    fi

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

    # Configure provisioner to start up when the box is restarted.
    echo "Configuring provisioner to run on startup." >> ${logfile}
    chkconfig --add provisioner 1>> ${logfile} 2>> ${logfile}
    if [ $? -ne 0 ]
    then
        echo "Error configuring provisioner to run on startup." >> ${logfile}
    fi


######################################################################
# The preun section lists actions to be performed before
# un-installation
#######################################################################
%preun
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

    if [ "$1" == "0" ]
    then
        echo "Removing provisioner from list of processes to run on startup." >> ${logfile}
        chkconfig --del provisioner 1>> ${logfile} 2>> ${logfile}
        if [ $? -ne 0 ]
        then
            echo "Error removing provisioner from list of processes to run on startup." >> ${logfile}
        fi
    fi

#######################################################################
# The postun section lists actions to be performed after
# un-installation
#######################################################################
%postun

