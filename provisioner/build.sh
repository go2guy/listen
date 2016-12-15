#!/bin/bash
cd `dirname $0`

if [ -z $GIT_BRANCH ]; then
    export GIT_BRANCH=`git rev-parse --abbrev-ref HEAD`
fi

if [ -z $JENKINS_VERSION ]; then
	export JENKINS_VERSION=99.0
fi

rpmbuild -bb -v provisioner.spec