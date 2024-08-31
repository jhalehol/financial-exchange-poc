#!/usr/bin/env bash

export DB_USERNAME="postgres"
export DB_PASSWORD="pass"
export DB_HOST="localhost:5432"
export DB_NAME="yellowpepper"
export CONVERTER_KEY="7fcf79177051a9b21642321713bb6b71"

./validate-auth-keys.sh

mvn clean spring-boot:run
