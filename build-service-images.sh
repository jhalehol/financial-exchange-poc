#!/usr/bin/env bash

API_SOURCE="api"
UI_SOURCE="ui"
CHECK_POINT="stack.build"

if [ ! -f "${CHECK_POINT}" ]; then
  # Build image for API
  echo "Building API image"
  pushd "${API_SOURCE}"
  ./build-image.sh
  popd

  #Build image for UI
  echo "Building UI image"
  pushd "${UI_SOURCE}"
  ./build-image.sh
  popd

  touch "${CHECK_POINT}"
fi



