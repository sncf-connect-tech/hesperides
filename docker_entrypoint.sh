#!/bin/sh
set -o pipefail -o errexit -o nounset

create_ok=$(mongo $PROJECTION_REPOSITORY_MONGO_URI --quiet --eval "db.createCollection('test').ok")
if [ $create_ok -ne 1 ]; then
    echo -e 'Not enough permissions to create a collection: connected to a non-PRIMARY node ?\nCheck that $PROJECTION_REPOSITORY_MONGO_URI contains replicaSet=...' >&2
    exit 1
fi
mongo $PROJECTION_REPOSITORY_MONGO_URI --eval "db.test.drop()"

mongo $PROJECTION_REPOSITORY_MONGO_URI mongo_create_collections.js

# If args were passed to this script, execute them as a command, else do nothing:
exec "${@:-true}"
