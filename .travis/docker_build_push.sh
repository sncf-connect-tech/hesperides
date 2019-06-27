#!/usr/bin/env bash

set -o pipefail -o errexit -o nounset -o xtrace

# Build a docker image and push it to docker hub (only when it's not a pull request)
if [ "$DOCKER_USER" != "" ] && [ "$DOCKER_PASS" != "" ]; then
    docker login -u $DOCKER_USER -p $DOCKER_PASS
    if [ "$TRAVIS_BRANCH" == "master" ]; then
        export TAG=latest
    else
        export TAG=$(echo $TRAVIS_BRANCH | sed -e 's/\//_/g' -e 's/\#//g' -e 's/\-/_/g')
    fi
    docker build -t hesperides/hesperides:$TAG --label git_commit=$TRAVIS_COMMIT --label date=$(date +%F) \
        --build-arg BUILD_TIME=$(date +%FT%T) --build-arg GIT_TAG=$(date +%F) --build-arg GIT_BRANCH=$TRAVIS_BRANCH --build-arg GIT_COMMIT=$TRAVIS_COMMIT --build-arg GIT_COMMIT_MSG=$TRAVIS_COMMIT_MESSAGE .
    echo "✓ Docker image built"
    docker push hesperides/hesperides:$TAG
    echo "✓ Docker image pushed to public hub with version $TAG"
    docker tag hesperides/hesperides:$TAG hesperides/hesperides:$(date +%F)
    docker push hesperides/hesperides:$(date +%F)
    echo "✓ Docker image pushed to public hub with version $(date +%F)"
else
    echo '✗ Missing $DOCKER_USER or $DOCKER_PASS environment variable'
fi