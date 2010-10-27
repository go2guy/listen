#!/bin/bash

if [ $# -ne 1 ]; then
    echo "Usage: $0 <destinationFile>"
    exit 1
fi

openssl genrsa -out $1 2048