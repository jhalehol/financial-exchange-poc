#!/usr/bin/env bash

PRIVATE_KEY_FILE="key"
PUBLIC_KEY_FILE="key_.pub"

echo "Validating authentication keys for JWT token"

function generateKeys() {
  echo "Generating keys ${PRIVATE_KEY_FILE} and ${PUBLIC_KEY_FILE}"
  # Generate private and public keys
  ssh-keygen -m PEM -t rsa -b 4096 -f "${PRIVATE_KEY_FILE}" -q -N ""

  # Convert format of public key to PEM format
  ssh-keygen -f "${PRIVATE_KEY_FILE}.pub" -e -m pkcs8 > "${PUBLIC_KEY_FILE}"

# Remove public key copy
rm "${PRIVATE_KEY_FILE}.pub"
}

if [ ! -f "${PRIVATE_KEY_FILE}" ] || [ ! -f "${PUBLIC_KEY_FILE}" ] ; then
   generateKeys
fi
