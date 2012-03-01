#!/bin/bash
#
#
#  Master build shell script
#
cd `dirname $0`

if [ `rpm -q uia-pack &>/dev/null; echo $?` -ne 0 ]
then
    echo -e "\nThe build requires the uia-pack rpm to be installed on this system.\nThis package can be located through jenkins on the 'Apps' tab under the appropriate uia project.\n"
    exit 1
fi

/opt/pack/pack.py --name="listen" --version="0.2" --interface="./ii_artifacts/listen.py" --manifest="./files.mf" --manifest-search-path="../../"
