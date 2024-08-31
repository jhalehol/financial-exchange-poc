#!/usr/bin/env bash

set -e

# Environment variables provided to allow run stack for test not for production
export DB_PASSWORD=yellowpepper
export DB_NAME=yellopepper_financial
export DB_USERNAME=yellowpepper
export CONVERTER_KEY="7fcf79177051a9b21642321713bb6b71"
if [ ! -f "${CHECK_POINT}" ]; then
  echo "Check point for build images not found, building images from source"
  ./build-service-images.sh
fi

docker-compose up
