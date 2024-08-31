#!/usr/bin/env bash

API_URL="http://localhost:9200/"
USERS_DATA="users-example-data.json"
TRANSFERS_DATA_FILE="transfers-example-data.json"

echo "Getting JWT token for admin"
admin_token=$(curl -X POST "${API_URL}oauth2/token" --header 'Content-Type: application/json' --data-raw '{"username": "admin","password": "admin"}' | jq '.token')
temp="${admin_token%\"}"
admin_token="${temp#\"}"
echo "token: ${admin_token}"
function create_user() {
  data_raw=$1
  curl --location --request POST "${API_URL}api/users/add" --header "Authorization: bearer ${admin_token}" --header 'Content-Type: application/json' --data-raw "${data_raw}"
  echo ""
}

function fill_users() {
  echo "Creating users:"
  while IFS=':' read -r data_raw
  do
    create_user "${data_raw}"
  done < "${USERS_DATA}"
}

function fill_transfers() {
  echo "Filling transfers"
  data_raw=$(cat $TRANSFERS_DATA_FILE)
  curl --location --request POST "${API_URL}api/transfer/populate" --header "Authorization: bearer ${admin_token}" --header 'Content-Type: application/json' --data-raw "${data_raw}"
}

fill_users && fill_transfers
