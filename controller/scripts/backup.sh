#!/bin/bash

if [ $# == 0 ]; then
    echo "Please provide a destination tgz filename" >&2
    exit 1
fi

DATE=`date +%Y%m%d%H%M%S`
DESTFILE=$1

TMPDIR=/tmp/backup-live-for-stage-$DATE
mkdir -p $TMPDIR

mysqldump --user=root --password=super listen > $TMPDIR/db.sql
tar cvzf $TMPDIR/artifacts.tgz -C /var/www/html/listen artifacts

tar cvzf $DESTFILE -C $TMPDIR artifacts.tgz db.sql
