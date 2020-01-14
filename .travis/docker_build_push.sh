#!/usr/bin/env bash

set -o pipefail -o errexit -o nounset -o xtrace

# Build a docker image and push it to docker hub (only when it's not a pull request)
if [ "$DOCKER_USER" != "" ] && [ "$DOCKER_PASS" != "" ]; then
    docker login -u $DOCKER_USER -p $DOCKER_PASS
    if [ "$TRAVIS_BRANCH" == "master" ]; then
        TAG=latest
    else
        TAG=$(echo $TRAVIS_BRANCH | sed -e 's~/~_~g' -e 's/#//g' -e 's/-/_/g')
    fi
    docker tag hesperides/hesperides:$TRAVIS_COMMIT hesperides/hesperides:$TAG
    echo "✓ Docker image hesperides/hesperides:$TRAVIS_BRANCH tagged: $TAG"
    docker push hesperides/hesperides:$TAG
    echo "✓ Docker image pushed to public hub with version $TAG"
    docker tag hesperides/hesperides:$TAG hesperides/hesperides:$(date +%F)
    docker push hesperides/hesperides:$(date +%F)
    echo "✓ Docker image pushed to public hub with version $(date +%F)"
else
    echo '✗ Missing $DOCKER_USER or $DOCKER_PASS environment variable'
fi
