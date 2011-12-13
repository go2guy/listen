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
Requires: spotbuild-vip, ghostscript, mysql-server

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
#    cd ../spotbuild/rhinounit/
#    /usr/local/ant/bin/ant
#    if [ $? -ne 0 ]
#    then
#        echo -e "\nJava script unit tests failed!\n"
#    else
#        echo -e "\nPassed all Javascript tests\n"
#    fi
#    cd -
    cd ../wavconvert
    make
    if [ $? -ne 0 ]
    then
    	echo -e "\nFailed to create logtolinwav\n"
    fi
    cd -

#######################################################################
# The install section is used to install the code into directories
# on the build machine
#######################################################################
%install
    echo "%{name} will now be built and installed."

    # Install vxml & ccxml scripts
    mkdir -p %{buildroot}/interact/apps/
    mkdir -p %{buildroot}/var/www/html/ippbx/
    mkdir -p %{buildroot}/interact/listen/artifacts/
    mkdir -p %{buildroot}/interact/listen/bin/
    mkdir -p %{buildroot}/interact/listen/artifacts/afterHrs/
    mkdir -p %{buildroot}/var/www/html/interact/listen/

    cp -r %{STARTDIR}/spotbuild %{buildroot}/interact/apps
    cp -r %{STARTDIR}/ippbx %{buildroot}/interact/apps/spotbuild
    cp -r %{STARTDIR}/broadcast %{buildroot}/interact/apps/spotbuild
    cp -r %{STARTDIR}/directMessage %{buildroot}/interact/apps/spotbuild
    cp -r %{STARTDIR}/msgLightCntrl %{buildroot}/interact/apps/spotbuild
    cp -r %{STARTDIR}/ippbx/php/* %{buildroot}/var/www/html/ippbx/
    cp -r %{STARTDIR}/wavconvert/logtolinwav %{buildroot}/interact/listen/bin/

    # Remove extras
    rm -rf %{buildroot}/interact/apps/spotbuild/*.docx
    rm -rf %{buildroot}/interact/apps/spotbuild/ippbx/php
    rm -rf %{buildroot}/interact/apps/spotbuild/after_hours
    rm -rf %{buildroot}/interact/apps/spotbuild/rhinounit

    # Run Encryption
    /interact/program/iiXMLcrypt -e "Listen" %{buildroot}/interact/apps/spotbuild/

    # Add root.vxml
    for rootfile in `find %{STARTDIR}/spotbuild/listen* -name root.vxml`
    do
        listendir=`dirname ${rootfile}`
        listendir=`basename ${listendir}`
        /bin/cp ${rootfile} %{buildroot}/interact/apps/spotbuild/${listendir}/
    done

    /bin/cp %{STARTDIR}/ippbx/root.vxml %{buildroot}/interact/apps/spotbuild/ippbx/

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
    /interact/apps/spotbuild/listen*
    /interact/apps/spotbuild/ippbx*
    /interact/apps/spotbuild/broadcast*
    /interact/apps/spotbuild/directMessage*
    /interact/apps/spotbuild/msgLightCntrl*
    /interact/apps/spotbuild/lib/cgi-bin/listen
    /interact/listen/artifacts
    /interact/listen/bin/logtolinwav
    /var/www/html/interact/listen
    /var/www/html/ippbx

    %config /interact/apps/spotbuild/listen_defaultDial/root.vxml
    %config /interact/apps/spotbuild/listen_autoDial/root.vxml
    %config /interact/apps/spotbuild/listen_main/root.vxml
    %config /interact/apps/spotbuild/listen_voicemail/root.vxml
    %config /interact/apps/spotbuild/listen_conference/root.vxml
    %config /interact/apps/spotbuild/listen_confEvents/root.vxml
    %config /interact/apps/spotbuild/listen_findmeAdmin/root.vxml
    %config /interact/apps/spotbuild/listen_transcription/root.vxml
    %config /interact/apps/spotbuild/listen_artifacts/root.vxml
    %config /interact/apps/spotbuild/listen_autoAttendant/root.vxml
    %config /interact/apps/spotbuild/listen_record/root.vxml
    %config /interact/apps/spotbuild/listen_findme/root.vxml
    %config /interact/apps/spotbuild/listen_mailbox/root.vxml
    %config /interact/apps/spotbuild/listen_afterHours/root.vxml
    %config /interact/apps/spotbuild/ippbx/root.vxml

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

    echo "iistart link currently points to [ `readlink /interact/apps/iistart.ccxml` ]. It will be wiped out and re-linked to [ /interact/apps/spotbuild/listen_main/listen_main.ccxml ]." >> ${debug}
    rm -f /interact/apps/iistart.ccxml >> ${debug} 2>> ${error}
    ln -s /interact/apps/spotbuild/listen_main/listen_main.ccxml /interact/apps/iistart.ccxml >> ${debug} 2>> ${error}

    rm -f /var/www/html/interact/listen/artifacts >> ${debug} 2>> ${error}
    ln -s /interact/listen/artifacts /var/www/html/interact/listen/artifacts >> ${debug} 2>> ${error}

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

        # Remove artifacts link
        rm -f /var/www/html/interact/listen/artifacts

        # Remove lame link
        if [ -L /interact/listen/bin/lame ]
        then
            rm -f /interact/listen/bin/lame
        fi
    fi

