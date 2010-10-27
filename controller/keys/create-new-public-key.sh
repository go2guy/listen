#!/bin/bash

if [ $# -ne 1 ]; then
    echo "Usage: $0 <publicKeyFile>"
    exit 1
fi

openssl rsa -pubout -in privateKey.pem -out $1