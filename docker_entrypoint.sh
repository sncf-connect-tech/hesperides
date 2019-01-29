#!/bin/sh
set -o pipefail -o errexit -o nounset

mongo $PROJECTION_REPOSITORY_MONGO_URI mongo_create_collections.js

# If args were passed to this script, execute them as a command, else do nothing:
exec "${@:-true}"
