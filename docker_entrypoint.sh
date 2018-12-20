#!/bin/sh
set -o pipefail -o errexit -o nounset

cd "${BASH_SOURCE[0]}"

mongo $PROJECTION_REPOSITORY_MONGO_URI mongo_create_collections.js

exec "${@:-true}"
