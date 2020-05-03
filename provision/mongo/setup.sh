#!/usr/bin/env bash

echo "Setting up Database!"
set -e
CLI_ERR_MSG="Mongodb CLI tools not available (mongo). Aborting."
hash mongo 2>/dev/null || { echo >&2 $CLI_ERR_MSG ; exit 1; }

DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"

cd $DIR

## Create users with admin roles
mongo "$DIR/helpers/create-admin-users.js"

echo ""
echo "----------"
echo "Done!"
