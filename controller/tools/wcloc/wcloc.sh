#!/bin/bash

# counts lines under a specified directory
# the DIR array used below specified subdirectories to include
# the following file extensions are checked:
#  .java
#  .jsp
#  .sh
#  .xml
#  .js
#  .css
#  .pl

# if an argument is passed after the ROOTDIR argument the output will
# be tailored for Hudson plotting

if [ $# -eq 0 ]
    then
    echo "Missing ROOTDIR argument"
    exit 1
fi

ROOTDIR=$1

if [ ! -d $ROOTDIR ]
    then
    echo "$ROOTDIR is not a directory"
    exit 1
fi

FORMAT=0

if [ $# -gt 1 ]
    then
    FORMAT=$2
fi

DIR[0]=$ROOTDIR/src/java
DIR[1]=$ROOTDIR/test/junit/src
DIR[2]=$ROOTDIR/SMPTools/src/share
DIR[3]=$ROOTDIR/src/web/aperture
DIR[4]=$ROOTDIR/src/web/axis
DIR[5]=$ROOTDIR/src/web/customercare
DIR[6]=$ROOTDIR/src/web/iiAdmin
DIR[7]=$ROOTDIR/src/web/iiPayment
DIR[8]=$ROOTDIR/src/web/Metrics
DIR[9]=$ROOTDIR/src/web/smp_jsecure
DIR[10]=$ROOTDIR/src/web/_all/javascript/ii-utils.js
DIR[11]=$ROOTDIR/src/web/_all/javascript/interact-datetime.js
DIR[12]=$ROOTDIR/src/web/_all/jsp
DIR[13]=$ROOTDIR/src/web/_all/tld
#DIR[4]=$ROOTDIR/rateadmin/src
#DIR[5]=$ROOTDIR/rateadmin/test/src/java/test

SUM=0

for f in `find ${DIR[@]} -name '*.java' -or -name '*.jsp' -or -name '*.sh' -or -name '*.xml' -or -name '*.js' -or -name '*.css' -or -name '*.pl' -or -name '*.tld'`;
  do
  NUM=`wc -l $f | cut -d' ' -f 1`;
  SUM=$(($SUM + $NUM));
done

if [ $FORMAT -eq 1 ]
    then
    echo "YVALUE=$SUM"
else
    echo $SUM
fi
