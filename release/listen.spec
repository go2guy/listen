#######################################################################
# Interact.spec
#######################################################################
%define STARTDIR %(pwd)

%ifnarch x86_64
%define _arch i686
%endif

Summary: Contains a full release of the Interact listen Software.
Name: listen
Version: %(echo ${VERSION})
Release: %(echo ${RELEASE})
License: Copyright (c) Interact Incorporated. All Rights Reserved.
Vendor: Interact Incorporated
URL: http://www.iivip.com
Distribution: %(cat /etc/issue | head -1)
Packager: Interact Incorporated <support@iivip.com>
Group: Interact
BuildRoot: %{STARTDIR}/BUILD
BuildArch: %{_arch}

# So the package cannot be installed directly.
Requires: USE_IIUIA_TO_INSTALL

%define __spec_install_post /usr/lib/rpm/brp-compress
%define manifest %( echo $FILEFILE )

%define _topdir %(if [ "${TOPDIR}" == "" ]; then echo "/var/tmp/%{name}-%{version}-%{release}-topdir"; else echo "${TOPDIR}"; fi;)
%(mkdir -p %{_topdir}/RPMS/)
%(mkdir -p %{_topdir}/SRPMS/)
%(mkdir -p %{_topdir}/BUILD/)
%(mkdir -p %{_topdir}/SOURCES/)

#######################################################################
# The description section is used to explain what the package does,
# describe any warnings or additional configuration instructions,
# etc. Anyone using rpm -qi to query your package will be given
# this description.
#######################################################################
%description
  Contains %{name}-%{version}-%{release} packages and configuration questions.

#######################################################################
# The prep command section is used to unpack the source code into a
# temporary directory and apply any patches.
#######################################################################
%prep
    rm -rf %{_topdir}/RPMS/*
    rm -rf %{_topdir}/SRPMS/*

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
    %defattr(755,interact,operator)
    /interact/packages/ 

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

