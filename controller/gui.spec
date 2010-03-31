#######################################################################
# gui.spec
#######################################################################
%define STARTDIR %(pwd)

# Make sure we specify i686 if this is not a 64 bit build.
%ifnarch x86_64
%define _arch i686
%endif

Summary: Interact Listen GUI
Name: listen-gui
Version: %(echo "${VERSION}")
Release: %(echo "${RELEASE}")
License: Copyright (c) Interact Incorporated. All Rights Reserved.
Group: Interact
URL: http://www.iivip.com
Vendor: Interact Incorporated
Packager: javagroup <javagroup@iivip.com>
BuildArch: %{_arch}
BuildRequires: jdk >= 1.6.0_00

%define __spec_install_post /usr/lib/rpm/brp-compress
%define _topdir %(echo "${TOPDIR}")
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
    This RPM will install/uninstall the Listen GUI.

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
    /interact/listen


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
ln -s /interact/listen/scripts/listen-gui /etc/init.d/listen-gui
chkconfig --add listen-gui


#######################################################################
# The preun section lists actions to be performed before
# un-installation
#######################################################################
%preun
if [ "$1" -le "0" ]; then
  chkconfig --del listen-gui
  rm -f /etc/init.d/listen-gui
fi



#######################################################################
# The postun section lists actions to be performed after
# un-installation
#######################################################################
%postun

