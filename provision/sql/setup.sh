#!/usr/bin/env bash

echo "Setting up Database!"
set -e
CLI_ERR_MSG="Postgres CLI tools not available (psql). Using Postgres.app, look
at http://postgresapp.com/documentation/cli-tools.html. Aborting."
hash psql 2>/dev/null || { echo >&2 $CLI_ERR_MSG ; exit 1; }

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

cd $DIR

# Provision

## Create roles
psql -f $DIR/helpers/create-role.sql

## Create dbs
psql -f $DIR/helpers/create-db.sql

echo ""
echo "----------"
echo "Done!"
