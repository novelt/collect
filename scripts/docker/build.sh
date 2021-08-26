#!/usr/bin/env bash

docker buildx build \
    --tag "${DOCKER_IMAGES_PREFIX}gts_mobile:${APP_VERSION}" \
    .