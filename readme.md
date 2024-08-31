# Transfers Viewer

### Components

The application is composed by two main micro-services API and the UI that can run independently but
to allow use the UI with all the functionality the API should be running to consume services.

Architecture diagrams located in /arch folder:

* data_model.jpg (Database diagram)
* deployment-model.jpg (Deployment diagram for current stack)


To run the application you can use some of these approaches

###### Security Layers:

* UI requires basic authentication user/password authentication to access to transfers of the user, this is user based not allowing to
view another user accounts.
* API requires Authorization JWT token for all requests (except to retrieve the token), those requests are secured
role-based and some of them are only allowed for admin users.
  
Allowed operations for admin Role
* /api/transfer/populate
* /api/users/add

Allowed operations for user Role
* /api/currencies/supported
* /api/transfer/

All users with correct credentials
* /oauth2/token

### Running complete stack (easy and fast)

###### Requirements

* Docker (Supporting docker-compose command)
* Maven 3+

You can run the complete stack of the application using docker-compose command without DB configuration and
using current docker compose configuration provided in the root path, to run the stack simply run the provided script:

`
./run-stack.sh
`

This will create a service stack with postgres database, UI and API running, to access to those services
use the next URLs:

* API: http://localhost:9200/api
* UI: http://localhost:8080


By default the application for test purposes has enabled the user admin with the credentials, this user
contains admin privileges:

* username: admin
* password: admin

To fill some example data with some users, accounts and transfers you can run the script:

You need to run this in another terminal window located in the same path

`
./fill-example-data.sh
`

After this you can access to the UI with some of the next users to see the transfers of every user account:


- username: jhon / password: jhon
- username: louise / password: louise
- username: peter / password: peter

### Running service by service without stack (service by service)

###### Requirements

* PostgresDB
* Maven +3
* Angular Client
* Node
* Npm or Yarn

###### Running API

To run locally the API you can use the maven command to run the aplication but you need to set some environment
variables please go to the api/ folder and check the `run-dev.sh` script and change the variables with your data

Running the API from folder api/:

`./run-dev.sh`

To run locally the UI you should use the ng commands to run in developer mode the application, to run it
run:

Located in the ui/ folder run commands:

###### Installing dependencies

`
npm install
`

or using Yarn

`
npm yarn
`

###### Running the server 

`
ng serve
`

Same from the previous documentation you can fill some data in the DB using the `./fill-example-data.sh` script
but remember that you require the API running


