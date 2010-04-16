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
elif [ "${controlserver}" == "" ]
then
    echo "No control server host given."
    exit 1
elif [ "${spotserver}" == "" ]
then
    echo "No spot server host given."
    exit 1
elif [ "${webserver}" == "" ]
then
    echo "No web server host given."
    exit 1
else
    echo
    echo "Servers to be deployed:"
    echo "Control server               [ $controlserver ]."
    echo "Spot server                  [ $spotserver ]."
    echo "Web server                   [ $webserver ]."
    echo
fi

# Get upstream artifact
wget -nv ${UPSTREAM_URL}/artifact//*zip*/listen.zip
unzip listen.zip
export listenRPM=`find ./archive -name "INSA*.rpm"`

# Get last successful uia
wget -nv ${UIA_URL}/artifact//*zip*/uia.zip
unzip uia.zip
export UIARPM=`find ./archive -name "uia*.rpm"`

echo "listenRPM is [ ${listenRPM} ]."
echo "UIARPM is [ ${UIARPM} ]."

# Distribute packages to server
#scp ${SSH_OPTS} ${INSARPM} ${UIARPM} root@${INSASERVER}:/interact/
#scp ${SSH_OPTS} deployINSA.py root@${INSASERVER}:/root/

# Invoke deploy -- do all phases
#ssh ${SSH_OPTS} root@${INSASERVER} "/root/deployINSA.py --insaserver=${INSASERVER} --rtbserver=${RTBSERVER} --dbserver=${DBSERVER} --webserver=${WEBSERVER} --phase=all"


#rm -f /interact/packages/listen*rpm
#
#cp listen/release/ii_artifacts/*.rpm /interact/
#
#listen/autodeploy/deploy_tool.py --controlserver=listenvip.interact.nonreg --spotserver=listenvip.interact.nonreg --webserver=listenvip.interact.nonreg --phase=all

