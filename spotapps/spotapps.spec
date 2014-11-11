#######################################################################
# spotapps.spec
#######################################################################
%define STARTDIR %(pwd)
%define _arch noarch

Summary: NewNet Communication Technologies Listen SPOT Applications package
Name: listen-spotapps
Version: 7.0
Release: %(svnversion | cut -d ':' -f 1)
License: Copyright (c) NewNet Communication Technologies. All Rights Reserved.
Group: Interact
Vendor: NewNet Communication Technologies
URL: http://www.newnet.com
Packager: applications <applications@newnet.com>
BuildArch: %{_arch}
BuildRoot: %{STARTDIR}/BUILD/%{name}-%{version}-%{release}-buildroot

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
    NewNet Communication Technologies Listen SPOT Applications package.

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

    # Install php scripts
    mkdir -p %{buildroot}/interact/apps/spotbuild/lib/cgi-bin/listen
    cp %{STARTDIR}/scripts/*php %{buildroot}/interact/apps/spotbuild/lib/cgi-bin/listen/
    cp %{STARTDIR}/scripts/getPrompts.php %{buildroot}/var/www/html/interact/listen/
    rm -rf %{buildroot}/interact/apps/spotbuild/lib/cgi-bin/listen/getPrompts.php

    rm -rf `find %{buildroot}/ -name ".svn" -type d`

#######################################################################
# The files section lists all files included in the RPM
#######################################################################
%files
    # We want to exclude the /interact/apps/spotbuild/ and /interact/apps/spotbuild/lib/cgi-bin/ directories and only include contents under them
    # This is to keep the package from colliding with the directories which are also delivered by spobtuild-vip
    %defattr(777,interact,operator)
    /interact/apps/spotbuild
    /var/www/html/interact/listen
    /var/www/html/ippbx

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
    cd %{STARTDIR}/wavconvert
    make clean


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

    if [ ! -f /usr/local/bin/lame ]
    then
        echo "******ALERT******"
        echo -e "\tlame is not installed under [/usr/local/bin/]. Install the application to enable conversation of audio files from wav to mp3."
        echo "*****************"
    else
       echo "lame is installed under [/usr/local/bin/]"
       if [ ! -L /interact/listen/bin/lame ]
       then
         echo -e "creating link soft link [/interact/listen/bin/lame -> /usr/local/bin/lame]"
         ln -s /usr/local/bin/lame /interact/listen/bin/lame >> ${debug} 2>> ${error}
       fi
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

        # Remove lame link
        if [ -L /interact/listen/bin/lame ]
        then
            rm -f /interact/listen/bin/lame
        fi
    fi

