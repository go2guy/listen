#!/bin/bash
#
#
#  Master build shell script
#  
#  This will do a lot of file copies, gathering
#  everything from files.mf, using the locations
#  in locations.mf as places to look, and then
#  invoke the rpmbuild to make them an Invigorate rpm

cd `dirname $0`

BUILDROOT=$1
if [ -z $BUILDROOT ]; then
  BUILDROOT=`pwd`/BUILD
fi

echo "Master build populating BUILDROOT=$BUILDROOT"
if [ ! -d $BUILDROOT/interact/packages ]; then
  mkdir -p $BUILDROOT/interact/packages/conf
else
  rm -rf $BUILDROOT/interact
  mkdir -p $BUILDROOT/interact/packages/conf
fi

# Create "find" list based on files.mf file.
FILESEARCH=""
for file in `egrep -v "^[[:space:]]*(#|!|$)" files.mf`
do
    if [ "${FILESEARCH}" == "" ]
    then
        FILESEARCH="-regex \".*/ii_artifacts/`basename ${file} | sed -re "s@\*@\.\*@g"`\""
    else
        FILESEARCH="${FILESEARCH} -o -regex \".*/ii_artifacts/`basename ${file} | sed -re "s@\*@\.\*@g"`\""
    fi
done

# Search all locations for the files in files.mf and copy any found into the buildroot.
cat locations.mf | egrep -v "^[[:space:]]*(#|!|$)" | \
    while read location
    do
        echo "Processing location [ ${location} ]."
        case "${location}" in
        *":/"*)
            # Case for handling manifest entries that are meant to be scp'd into the buildroot.
            # Parse host name from location.
            host="`echo "${location}" | awk -F":/" '{print $1}'`"

            # Parse path from location and escape special characters.
            path="`echo "${location}" | awk -F"${host}:" '{print $2}' | sed -re "s@([[:space:]()])@\\\\\\\\\1@g"`"

            # Execute a find command on the remote location searching for all files in the files.mf list.
            files="`ssh ${host} "find ${path} ${FILESEARCH}"`"

            # Separate the rpm and config files as they go in separate directories.
            rpmfiles="`echo "${files}" | egrep "rpm$"`"
            confiles="`echo "${files}" | egrep -v "rpm$"`"

            # If rpm files were found, scp them back.
            if [ "${rpmfiles}" != "" ]
            then
                # put the host name before each of the files to be scp'd
                rpmfiles="`echo ${rpmfiles} | sed -re "s@(^|[[:space:]])/@\1${host}:/@g"`"
                echo "Found rpm file(s) [ ${rpmfiles} ]."

                # get the rpms from the remote location and put them in the build root.
                scp ${rpmfiles} $BUILDROOT/interact/packages/ &>/dev/null
            fi

            # If config files were found, scp them back.
            if [ "${confiles}" != "" ]
            then
                # put the host name before each of the files to be scp'd
                confiles="`echo ${confiles} | sed -re "s@(^|[[:space:]])/@\1${host}:/@g"`"
                echo "Found config file(s) [ ${confiles} ]."

                # get the config files from the remote location and put them in the build root.
                scp ${confiles} $BUILDROOT/interact/packages/conf/ &>/dev/null
            fi
        ;;

        "/"*|".")
            # Execute a find command searching for all files in the files.mf list.
            files="`eval find ${location} ${FILESEARCH}`"

            # Separate the rpm and config files as they go in separate directories.
            rpmfiles="`echo "${files}" | egrep "rpm$"`"
            confiles="`echo "${files}" | egrep -v "rpm$"`"

            # If rpm files were found, cp them into the build root.
            if [ "${rpmfiles}" != "" ]
            then
                echo "Found rpm file(s) [ ${rpmfiles} ]."

                # get the rpms and put them in the build root.
                cp ${rpmfiles} $BUILDROOT/interact/packages/ &>/dev/null
            fi

            # If config files were found, cp them into the build root.
            if [ "${confiles}" != "" ]
            then
                echo "Found config file(s) [ ${confiles} ]."

                # get the config files and put them in the build root.
                cp ${confiles} $BUILDROOT/interact/packages/conf/ &>/dev/null
            fi
        ;;

        *)
            echo
            echo "*** ERROR ***"
            echo "Skipping unknown location format [ ${location} ]."
            echo "*************"
            echo
        ;;
        esac
    done

# Validate uia files against dtd...
echo
echo "Validating uia files against the dtd:"
retval=0
for uiafile in `find ${BUILDROOT}/ -name "*.uia"`
do
    echo "Validating [ ${uiafile} ]:"
    /opt/uia-validator/validate.py ${uiafile}
    if [ $? -ne 0 ]
    then
        retval=`expr ${retval} + 1`
    fi
    echo
done

if [ ${retval} -ne 0 ]
then
    echo "Uia validation failed for [ ${retval} ] file(s). Exiting."
    echo
    exit 1
fi

# Move iiInstall and iiLoader config files into place
cp iiInstall.cfg $BUILDROOT/interact/packages/conf/

export MANIFEST=`pwd`/files.mf
if [ -z "$VERSION" ]; then
    export VERSION="unknown"
fi

if [ -z "$RELEASE" ]; then
    export RELEASE="`date +%Y%m%d%H%M`"
fi
rpmbuild -bb --buildroot $BUILDROOT listen.spec

