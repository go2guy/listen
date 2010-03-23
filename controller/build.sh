#!/bin/bash

DEPLOY_DIR=`dirname $0`/listen_deploy
echo "DEPLOYING to $DEPLOY_DIR"

ant -Ddeploy.dir=$DEPLOY_DIR test deploy rpm
