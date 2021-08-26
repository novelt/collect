#!/usr/bin/env bash

script_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
repo_root="${script_dir}/.."
output_dir="${repo_root}/out"

echo "repo_root: ${repo_root}"
echo "output_dir: ${output_dir}"

source "${repo_root}/config/local.env"

if [[ "${output_dir}" != '/' ]]; then
    rm -rf "${output_dir}"
fi

#echo "Run testReleaseUnitTest"
#docker run \
#    --env ANDROID_KEYSTORE_KEY_PASSWORD="${ANDROID_KEYSTORE_KEY_PASSWORD}" \
#    --env ANDROID_KEYSTORE_STORE_PASSWORD="${ANDROID_KEYSTORE_STORE_PASSWORD}" \
#    "${DOCKER_IMAGES_PREFIX}gts_mobile:${APP_VERSION}" \
#    bash -c "./gradlew clean && ./gradlew testReleaseUnitTest"
echo "Retrieving Test Results"
#DOCKER_SOURCE_CONTAINER=$(docker ps -a --filter="ancestor=${DOCKER_IMAGES_PREFIX}gts_mobile" -q --last 1)
#docker cp "${DOCKER_SOURCE_CONTAINER}":/usr/src/app/app/build "${output_dir}/"
#docker rm "${DOCKER_SOURCE_CONTAINER}"

#echo "Run testUatUnitTest"
#docker run \
#    --env ANDROID_KEYSTORE_KEY_PASSWORD="${ANDROID_KEYSTORE_KEY_PASSWORD}" \
#    --env ANDROID_KEYSTORE_STORE_PASSWORD="${ANDROID_KEYSTORE_STORE_PASSWORD}" \
#    "${DOCKER_IMAGES_PREFIX}gts_mobile:${APP_VERSION}" \
#    bash -c "./gradlew clean && ./gradlew testUatUnitTest"
echo "Retrieving Test Results"
#DOCKER_SOURCE_CONTAINER=$(docker ps -a --filter="ancestor=${DOCKER_IMAGES_PREFIX}gts_mobile" -q --last 1)
#docker cp "${DOCKER_SOURCE_CONTAINER}":/usr/src/app/app/build "${output_dir}/"
#docker rm "${DOCKER_SOURCE_CONTAINER}"

echo "Prepare local output"
rm -rf "${output_dir}"
mkdir -p "${output_dir}"

echo "Building APK's"
docker run \
    --env ANDROID_KEYSTORE_KEY_PASSWORD="${ANDROID_KEYSTORE_KEY_PASSWORD}" \
    --env ANDROID_KEYSTORE_STORE_PASSWORD="${ANDROID_KEYSTORE_STORE_PASSWORD}" \
    "${DOCKER_IMAGES_PREFIX}gts_mobile:${APP_VERSION}" \
    bash -c "./gradlew clean && ./gradlew assembleGtsUat assembleGtsRelease"

echo "Retrieving APKS"

DOCKER_SOURCE_CONTAINER=$(docker ps -a --filter="ancestor=${DOCKER_IMAGES_PREFIX}gts_mobile" -q --last 1)
docker cp "${DOCKER_SOURCE_CONTAINER}":/usr/src/app/app/build "${output_dir}/"
docker rm "${DOCKER_SOURCE_CONTAINER}"

echo "The build output is available here: ${output_dir}/"