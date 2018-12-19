#!/usr/bin/env bash

set -o pipefail -o errexit -o nounset

# Build a docker image and push it to docker hub (only when it's not a pull request)
if [ "$DOCKER_USER" != "" ] && [ "$DOCKER_PASS" != "" ]; then
    docker login -u $DOCKER_USER -p $DOCKER_PASS
    export TAG=`if [ "$TRAVIS_BRANCH" == "master" ]; then echo "latest"; else echo $TRAVIS_BRANCH | sed -e 's/\//_/g' -e 's/\#//g' -e 's/\-/_/g' ; fi`
    docker build -t hesperides/hesperides:$TAG --label git_commit=$COMMIT .
    docker push hesperides/hesperides:$TAG
    echo "✓ Docker image build and pushed to docker hub"
else
    echo '✗ Missing $DOCKER_USER or $DOCKER_PASS environment variable'
fi