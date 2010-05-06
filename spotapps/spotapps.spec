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
    cp -r %{STARTDIR}/spotbuild %{buildroot}/interact/apps

    # Remove extras and listen_conference root.vxml
    rm -rf %{buildroot}/interact/apps/spotbuild/*.docx %{buildroot}/interact/apps/spotbuild/*mailbox
    rm -rf %{buildroot}/interact/apps/spotbuild/*main %{buildroot}/interact/apps/spotbuild/*voicemail
    rm -f %{buildroot}/interact/apps/spotbuild/listen_conference/root.vxml

    # Run Encryption
    /interact/program/iiXMLcrypt -e listenConf %{buildroot}/interact/apps/spotbuild/listen_conference

    # Add root.vxml
    cp -r %{STARTDIR}/spotbuild/listen_conference/root.vxml %{buildroot}/interact/apps/spotbuild/listen_conference

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
    /interact/apps/spotbuild/lib/cgi-bin/listen

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
    # Update iistart.ccxml link if it is currently non-existant or pointing to iidefault.ccxml or welcome.ccxml
    if [ ! -f /interact/apps/iistart.ccxml ] || \
       [ "`readlink /interact/apps/iistart.ccxml`" == "/interact/apps/iidefault.ccxml" ] || \
       [ "`readlink /interact/apps/iistart.ccxml`" == "/interact/apps/spotbuild/welcome.ccxml" ]
    then
        rm -f /interact/apps/iistart.ccxml
        ln -s /interact/apps/spotbuild/listen_conference/listen_conference.ccxml /interact/apps/iistart.ccxml
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
    # If this is an uninstall and not an upgrade...
    if [ "$1" -le "0" ]
    then
        # Spotbuild apps are no longer valid
        rm -f /interact/apps/iistart.ccxml

        # Update iistart.ccxml to point back to either welcome.ccxml or iidefault.ccxml
        if [ -f /interact/apps/spotbuid/welcome.ccxml ]
        then
            ln -s /interact/apps/spotbuid/welcome.ccxml /interact/apps/iistart.ccxml
        elif [ -f /interact/apps/iidefault.ccxml ]
        then
            ln -s /interact/apps/iidefault.ccxml /interact/apps/iistart.ccxml
        fi
    fi

