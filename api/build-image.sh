#!/usr/bin/env bash

echo "Building jar"
mvn clean package

./validate-auth-keys.sh

IMAGE_NAME="financial-api"
docker build . -t ${IMAGE_NAME}

