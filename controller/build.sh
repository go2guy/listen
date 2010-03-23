#!/bin/bash

DEPLOY_DIR=`dirname $0`/listen_deploy

ant -Ddeploy.dir=/home/nick/listen/ test deploy
