#!/usr/bin/env bash

set -o pipefail -o errexit -o nounset -o xtrace

GIT_COMMIT_MSG="${1?'Required parameter'}"

sudo apt-get install -y ca-certificates wget

docker build -t $DOCKER_IMAGE:$GITHUB_SHA \
  --label date=$(date +%F) \
  --label git_commit=$GITHUB_SHA \
  --build-arg BUILD_TIME=$(date +%FT%T) \
  --build-arg GIT_TAG=$(date +%F) \
  --build-arg GIT_BRANCH=$GITHUB_REF \
  --build-arg GIT_COMMIT=$GITHUB_SHA \
  --build-arg GIT_COMMIT_MSG="$GIT_COMMIT_MSG" .

# We validate that the image built can start without any error:
docker run --rm -e SPRING_PROFILES_ACTIVE=noldap,fake_mongo -e EXIT_AFTER_INIT=true $DOCKER_IMAGE:$GITHUB_SHA
