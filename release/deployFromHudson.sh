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
elif [ "${controllerserver}" == "" ]
then
    echo "No listen controller server given."
    exit 1
elif [ "${spotserver}" == "" ]
then
    echo "No spot server given."
    exit 1
elif [ "${insaserver}" == "" ]
then
    echo "No insa server host given."
    exit 1
else
    echo
    echo "Servers to be deployed:"
    echo "Controller server           [ ${controllerserver} ]."
    echo "SPOT server                 [ ${spotserver} ]."
    echo "INSA server                 [ ${insaserver} ]."
    echo
fi

# Get upstream artifact
wget -nv ${UPSTREAM_URL}/artifact//*zip*/listen.zip && unzip listen.zip
if [ $? -ne 0 ]
then
    exit 1
fi

# Get last successful uia
wget -nv ${UIA_URL}/artifact//*zip*/uia.zip && unzip uia.zip
if [ $? -ne 0 ]
then
    exit 1
fi

export listenRPM=`find ./archive -name "listen-*.rpm"`
export UIARPM=`find ./archive -name "uia*.rpm"`

ssh ${SSH_OPTS} root@${listenserver} "rm -f /interact/packages/listen*rpm"
if [ $? -ne 0 ]
then
    exit 1
fi

scp ${listenRPM} ${UIARPM} ${listenserver}:/interact/
if [ $? -ne 0 ]
then
    exit 1
fi

scp deployListen.py ${listenserver}:/root/
if [ $? -ne 0 ]
then
    exit 1
fi


# Invoke deploy -- do all phases
ssh ${SSH_OPTS} root@${listenserver} "/root/deployListen.py --controllerserver=${controllerserver} --spotserver=${spotserver} --insaserver=${insaserver} --phase=all"
if [ $? -ne 0 ]
then
    exit 1
fi

