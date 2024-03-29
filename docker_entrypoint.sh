#!/bin/bash
set -o pipefail -o errexit -o nounset
if [ -z "${MONGO_URI:-}" ]; then
    if ! echo "${SPRING_PROFILES_ACTIVE:-}" | grep -qF fake_mongo; then
        echo 'Either $MONGO_URI must be defined or $SPRING_PROFILES_ACTIVE must contain "fake_mongo"' >&2
        exit 1
    fi
else
    echo "Creating Hesperides Test collection..."
    if ! mongosh $MONGO_URI mongo_test_create_collections.js; then
        echo -e 'Not enough permissions to create a collection: connected to a non-PRIMARY node ?\nCheck that $MONGO_URI contains replicaSet=...' >&2
        exit 1
    fi
    echo "Creating Hesperides collections..."
    mongosh $MONGO_URI mongo_create_collections.js
fi
# If args were passed to this script, execute them as a command, else do nothing:
exec "${@:-true}"
