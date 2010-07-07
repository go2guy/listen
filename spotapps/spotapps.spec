#######################################################################
# spotapps.spec
#######################################################################
%define STARTDIR %(pwd)
%define _arch noarch

Summary: Interact Incorporated Listen SPOT Applications package
Name: listen-spotapps
Version: 0.2
Release: %(svnversion | cut -d ':' -f 1)
License: Copyright (c) Interact Incorporated. All Rights Reserved.
Group: Interact
Vendor: Interact Incorporated
URL: http://www.iivip.com
Packager: applications <applications@iivip.com>
BuildArch: %{_arch}
BuildRoot: %{STARTDIR}/BUILD/%{name}-%{version}-%{release}-buildroot
Requires: spotbuild-vip

%define debug_package %{nil}

# Set up topdir area.
%define _topdir %{STARTDIR}
%(mkdir -p %{_topdir}/RPMS/%{buildarch})
%(mkdir -p %{_topdir}/SRPMS/)
%(mkdir -p %{_topdir}/BUILD/)
%(mkdir -p %{_topdir}/SOURCES/)

#######################################################################
# The description section is used to explain what the package does,
# describe any warnings or additional configuration instructions,
# etc. Anyone using rpm -qi to query your package will see be given
# this description.
#######################################################################
%description
    Interact Incorporated Listen SPOT Applications package.

#######################################################################
# The prep command section is used to unpack the source code into a
# temporary directory and apply any patches.
#######################################################################
%prep
    rm -rf %{_topdir}/RPMS/*
    rm -rf %{_topdir}/SRPMS/*
    rm -rf %{_topdir}/BUILD/*
    mkdir -p %{buildroot}/

#######################################################################
# The build section is used to compile the code.
#######################################################################
%build

#######################################################################
# The install section is used to install the code into directories
# on the build machine
#######################################################################
%install
    echo "%{name} will now be built and installed."

    # Install vxml & ccxml scripts
    mkdir -p %{buildroot}/interact/apps/
    mkdir -p %{buildroot}/var/www/html/ippbx/
    mkdir -p %{buildroot}/var/www/html/listen/artifacts/conference/record/
    mkdir -p %{buildroot}/var/www/html/listen/artifacts/conference/rollcall/
    mkdir -p %{buildroot}/var/www/html/listen/artifacts/voicemail/greeting/
    mkdir -p %{buildroot}/var/www/html/listen/artifacts/voicemail/message/

    cp -r %{STARTDIR}/spotbuild %{buildroot}/interact/apps
    cp -r %{STARTDIR}/ippbx %{buildroot}/interact/apps/spotbuild
    cp -r %{STARTDIR}/ippbx/php/*.php %{buildroot}/var/www/html/ippbx/

    # Remove extras
    rm -rf %{buildroot}/interact/apps/spotbuild/*.docx
    rm -rf %{buildroot}/interact/apps/spotbuild/ippbx/php

    # Run Encryption
    /interact/program/iiXMLcrypt -e "Listen" %{buildroot}/interact/apps/spotbuild/listen_main/
    /interact/program/iiXMLcrypt -e "Listen Conferencing" %{buildroot}/interact/apps/spotbuild/listen_conference/ %{buildroot}/interact/apps/spotbuild/listen_record/ %{buildroot}/interact/apps/spotbuild/listen_autoDial/
    /interact/program/iiXMLcrypt -e "Listen Voice Mail" %{buildroot}/interact/apps/spotbuild/listen_voicemail/ %{buildroot}/interact/apps/spotbuild/listen_mailbox/
    /interact/program/iiXMLcrypt -e "Listen Find Me" %{buildroot}/interact/apps/spotbuild/listen_findme/
    /interact/program/iiXMLcrypt -e "IP PBX" %{buildroot}/interact/apps/spotbuild/ippbx/

    # Add root.vxml
    for rootfile in `find %{STARTDIR}/spotbuild/listen* -name root.vxml`
    do
        listendir=`dirname ${rootfile}`
        listendir=`basename ${listendir}`
        /bin/cp ${rootfile} %{buildroot}/interact/apps/spotbuild/${listendir}/
    done

    /bin/cp %{STARTDIR}/spotbuild/ippbx/root.vxml %{buildroot}/interact/apps/spotbuild/ippbx/

    # Install php scripts
    mkdir -p %{buildroot}/interact/apps/spotbuild/lib/cgi-bin/listen
    cp %{STARTDIR}/scripts/*php %{buildroot}/interact/apps/spotbuild/lib/cgi-bin/listen/

    rm -rf `find %{buildroot}/ -name ".svn" -type d`

#######################################################################
# The files section lists all files included in the RPM
#######################################################################
%files
    # We want to exclude the /interact/apps/spotbuild/ and /interact/apps/spotbuild/lib/cgi-bin/ directories and only include contents under them
    # This is to keep the package from colliding with the directories which are also delivered by spobtuild-vip
    %defattr(777,interact,operator)
    /interact/apps/spotbuild/listen*
    /interact/apps/spotbuild/ippbx*
    /interact/apps/spotbuild/lib/cgi-bin/listen
    /var/www/html/listen/artifacts/*
    /var/www/html/ippbx/*

#######################################################################
# clean is a script that gets run at the end of the RPM building,
# if everything works, so that your temporary files don't hang
# around forever
#######################################################################
%clean
    rm -rf %{STARTDIR}/ii_artifacts/*.rpm
    mkdir -p %{STARTDIR}/ii_artifacts/
    mv -f "%{_topdir}/RPMS/%{buildarch}/%{name}-%{version}-%{release}.%{buildarch}.rpm" "%{STARTDIR}/ii_artifacts/%{name}-%{version}-%{release}.%{buildarch}.rpm"

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
    debug=/interact/packages/logs/debug
    if [ ! -d "`dirname ${debug}`" ]
    then
        debug=/dev/null
    fi

    info=/interact/packages/logs/info
    if [ ! -f "`dirname ${info}`" ]
    then
        info=/dev/null
    fi

    warn=/interact/packages/logs/warn
    if [ ! -f "`dirname ${warn}`" ]
    then
        warn=/dev/null
    fi

    error=/interact/packages/logs/error
    if [ ! -f "`dirname ${error}`" ]
    then
        error=/dev/null
    fi

    fatal=/interact/packages/logs/fatal
    if [ ! -f "`dirname ${fatal}`" ]
    then
        fatal=/dev/null
    fi

    echo "iistart link currently points to [ `readlink /interact/apps/iistart.ccxml` ]. It will be wiped out and re-linked to [ /interact/apps/spotbuild/listen_main/listen_main.ccxml ]." >> ${debug}
    rm -f /interact/apps/iistart.ccxml >> ${debug} 2>> ${error}
    ln -s /interact/apps/spotbuild/listen_main/listen_main.ccxml /interact/apps/iistart.ccxml >> ${debug} 2>> ${error}

    if [ ! -e /var/lib/mysql/ip_pbx ]
    then
        mysql -u root -v < /interact/apps/spotbuild/ippbx/sql/ippbx_schema.sql
    fi

#######################################################################
# The preun section lists actions to be performed before
# un-installation
#######################################################################
%preun
    
#######################################################################
# The postun section lists actions to be performed after
# un-installation
#######################################################################
%postun
    debug=/interact/packages/logs/debug
    if [ ! -d "`dirname ${debug}`" ]
    then
        debug=/dev/null
    fi

    info=/interact/packages/logs/info
    if [ ! -f "`dirname ${info}`" ]
    then
        info=/dev/null
    fi

    warn=/interact/packages/logs/warn
    if [ ! -f "`dirname ${warn}`" ]
    then
        warn=/dev/null
    fi

    error=/interact/packages/logs/error
    if [ ! -f "`dirname ${error}`" ]
    then
        error=/dev/null
    fi

    fatal=/interact/packages/logs/fatal
    if [ ! -f "`dirname ${fatal}`" ]
    then
        fatal=/dev/null
    fi

    # If this is an uninstall and not an upgrade...
    if [ "$1" -le "0" ]
    then
        echo "Uninstalling %{name}." >> ${debug}

        # iistart should currently be pointing to this app (which is no longer installed)
        rm -f /interact/apps/iistart.ccxml

        # Update iistart.ccxml to point back to either welcome.ccxml or iidefault.ccxml
        if [ -f /interact/apps/spotbuid/welcome.ccxml ]
        then
            echo "Linking iistart back to welcome."  >> ${debug}
            ln -s /interact/apps/spotbuid/welcome.ccxml /interact/apps/iistart.ccxml
        elif [ -f /interact/apps/iidefault.ccxml ]
        then
            echo "Linking iistart back to iidefault."  >> ${debug}
            ln -s /interact/apps/iidefault.ccxml /interact/apps/iistart.ccxml
        else
            echo "Can't relink iistart because iidefault does not exist."  >> ${debug}
        fi
    fi

