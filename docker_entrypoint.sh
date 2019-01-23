#!/bin/sh
set -o pipefail -o errexit -o nounset

cd /

mongo $PROJECTION_REPOSITORY_MONGO_URI mongo_create_collections.js

exec "${@:-true}"
