#!/usr/bin/env bash

set -o pipefail -o errexit -o nounset -o xtrace

if [ "$DOCKER_USER" != "" ] && [ "$DOCKER_PASS" != "" ]; then
  docker login -u "$DOCKER_USER" -p "$DOCKER_PASS"
  if [ "$GITHUB_REF" == "refs/heads/master" ]; then
    TAG=latest
  else
    TAG=$(echo "$GITHUB_REF" | sed -e 's~/~_~g' -e 's/#//g' -e 's/-/_/g')
  fi
  docker tag "$DOCKER_IMAGE":"$GITHUB_SHA" "$DOCKER_IMAGE":"$TAG"
  echo "✓ Docker image $DOCKER_IMAGE:$GITHUB_REF tagged: $TAG"
  docker push "$DOCKER_IMAGE":"$TAG"
  echo "✓ Docker image pushed to public hub with version $TAG"
  docker tag "$DOCKER_IMAGE":"$TAG" "$DOCKER_IMAGE":$(date +%F)
  docker push "$DOCKER_IMAGE":$(date +%F)
  echo "✓ Docker image pushed to public hub with version $(date +%F)"
else
  echo "✗ Missing $DOCKER_USER or $DOCKER_PASS environment variable"
fi
