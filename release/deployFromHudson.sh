#!/bin/bash

# For debug purposes
set -x
env

cd `dirname $0`

if [ "${UPSTREAM_URL}" == "" ]
then
    echo "No master package url given."
    exit 1
elif [ "${UIA_URL}" == "" ]
then
    echo "No uia package url given."
    exit 1
elif [ "${listenserver}" == "" ]
then
    echo "No listen server host given."
    exit 1
else
    echo
    echo "Servers to be deployed:"
    echo "Listen server               [ ${listenserver} ]."
    echo
fi

# Get upstream artifact
wget -nv ${UPSTREAM_URL}/artifact//*zip*/listen.zip
unzip listen.zip
export listenRPM=`find ./archive -name "listen-*.rpm"`

# Get last successful uia
wget -nv ${UIA_URL}/artifact//*zip*/uia.zip
unzip uia.zip
export UIARPM=`find ./archive -name "uia*.rpm"`

ssh ${SSH_OPTS} root@${listenserver} "rm -f /interact/packages/listen*rpm"
scp ${listenRPM} ${UIARPM} ${listenserver}:/interact/
scp deployListen.py ${listenserver}:/root/

# Invoke deploy -- do all phases
ssh ${SSH_OPTS} root@${listenserver} "/root/deployListen.py --listenserver=${listenserver} --phase=all"

