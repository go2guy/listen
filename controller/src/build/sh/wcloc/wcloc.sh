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

DIR[0]=$ROOTDIR/src/main/java
DIR[1]=$ROOTDIR/src/test/java

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
