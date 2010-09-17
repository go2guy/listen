#!/bin/bash

# WARNING! This is a destructive script. It will remove the database and
# audio archive files and reload them from a file. It is only intended for
# use with staging and development environments,
# -- NOT TO BE RUN IN LIVE. --

if [ $# != 2 ]; then
    echo "usage: restore-backup-onto-stage.sh <backup-file.tgz> <sanity-argument>" >&2
    exit 1
fi

if [ "$2" != "I am not running this on live" ]; then
    echo "Please read the script and make sure you know what you are doing" >&2
    exit 1
fi

DATE=`date +%Y%m%d%H%M%S`
TMPDIR=/tmp/restore-stage-from-live-$DATE
mkdir -p $TMPDIR

tar xvzf $1 -C $TMPDIR

service listen-controller stop

# delete and recreate archive audio files
rm -rf /var/www/html/listen
mkdir -p /var/www/html/listen
tar xvzf $TMPDIR/artifacts.tgz -C /var/www/html/listen

# drop and reload database
mysql --user=root -e "drop database listen"
mysql --user=root -e "create database listen"
mysql --user=root listen < $TMPDIR/db.sql

service listen-controller start