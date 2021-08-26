#!/usr/bin/env bash

docker buildx build \
    --tag "${DOCKER_IMAGES_PREFIX}gts_collect:${APP_VERSION}" \
    .