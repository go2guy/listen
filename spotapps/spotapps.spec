#######################################################################
# spotapps.spec
#######################################################################
%define STARTDIR %(pwd)
%define _arch noarch

Summary: Interact Incorporated Listen SPOT Applications package
Name: spotapps
Version: %(if [ "${VERSION}" == "" ]; then echo "1.0"; else echo "${VERSION}"; fi;)
Release: %(if [ "${RELEASE}" == "" ]; then date "+%Y%m%d%H%M"; else echo "${RELEASE}"; fi;)
License: Copyright (c) Interact Incorporated. All Rights Reserved.
Group: Interact
Vendor: Interact Incorporated
URL: http://www.iivip.com
Packager: applications <applications@iivip.com>
BuildArch: %{_arch}
BuildRoot: %{STARTDIR}/BUILD/%{name}-%{version}-%{release}-buildroot
Requires: spotbuild-vip

%define debug_package %{nil}
%define _repackage_dir /interact/packages/bkup/

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

    rm -rf `find %{buildroot}/ -name ".svn" -type d`

#######################################################################
# The files section lists all files included in the RPM
#######################################################################
%files
    %defattr(777,interact,operator)
    /interact/apps/spotbuild

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

    # Variables to help identify if this is an install or an upgrade.
    rpmexists=`rpm -q %{name} >/dev/null 2>/dev/null; echo $?`
    numrpms=`rpm -q %{name} | wc | awk '{print $1}'`

    #######################################################################
    # This section will only be run when actually installing the package
    # and will NOT be run when upgrading.
    #######################################################################

#######################################################################
# The post section lists actions to be performed after installation
#######################################################################
%post

     #######################################################################
     # This section will only be run when actually installing the package
     # and will NOT be run when upgrading.
     #######################################################################

    ######################################################################
    ### System Profile Configurations                                  ###
    ######################################################################


    ######################################################################
    ### Java Configurations                                            ###
    ######################################################################


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
    # Variables to help identify if this is an install or an upgrade.
    rpmexists=`rpm -q %{name} >/dev/null 2>/dev/null; echo $?`
    numrpms=`rpm -q %{name} | wc | awk '{print $1}'`

    #######################################################################
    # This section will only be run when actually installing the package
    # and will NOT be run when upgrading.
    #######################################################################
