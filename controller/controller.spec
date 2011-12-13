#######################################################################
# controller.spec
#######################################################################
%define STARTDIR %(pwd)
%define _arch noarch

Summary: Interact Listen Controller
Name: listen-controller
Version: %(echo "${VERSION}")
Release: %(echo "${RELEASE}")
License: Copyright (c) Interact Incorporated. All Rights Reserved.
Group: Interact
URL: http://www.iivip.com
Vendor: Interact Incorporated
Packager: javagroup <javagroup@iivip.com>
BuildArch: %{_arch}
Requires: xmlsecurity, iijava, mysql-server

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
    /interact/listen/lib/listen-controller.war
    %config /interact/listen/scripts/listen-controller

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
ln -sf /interact/listen/scripts/listen-controller /etc/init.d/listen-controller
chkconfig --add listen-controller


#######################################################################
# The preun section lists actions to be performed before
# un-installation
#######################################################################
%preun
if [ "$1" -le "0" ]; then
  chkconfig --del listen-controller
  rm -f /etc/init.d/listen-controller
fi



#######################################################################
# The postun section lists actions to be performed after
# un-installation
#######################################################################
%postun

