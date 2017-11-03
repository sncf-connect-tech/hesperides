#!/usr/bin/env bash

echo "DOCKER"

# Build a docker image and push it to docker hub (only when it's not a pull request)
if [ "$TRAVIS_PULL_REQUEST" == 'false' ]; then
    docker login -e $DOCKER_EMAIL -u $DOCKER_USER -p $DOCKER_PASS
    export TAG=`if [ "$TRAVIS_BRANCH" == "master" ]; then echo "latest"; else echo $TRAVIS_BRANCH | sed -e 's/\//-/g' -e 's/\#//g' ; fi`
    export IMAGE=hesperides/hesperides
    docker build -t $IMAGE:$COMMIT .
    docker tag $IMAGE:$COMMIT $IMAGE:$TAG
    docker push $IMAGE:$TAG

    echo "✓ Docker image build and pushed to docker hub"
else
    echo "✓ No docker image build on pull request"
fi