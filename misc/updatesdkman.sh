#!/usr/bin/env bash

set -e

mulefd_version=`ls build/distributions/mulefd-*.*.zip | sed -e 's/.*mulefd-\(.*\).zip/\1/g'`
echo "Updating mulefd sdkman with version $mulefd_version"


echo ${SDKMAN_CONSUMER_KEY} | cut -c-5
echo ${SDKMAN_CONSUMER_TOKEN} | cut -c-5

echo "Release on sdkman ${mulefd_version} from `pwd`"
curl -X POST \
    -H "Consumer-Key: ${SDKMAN_CONSUMER_KEY}" \
    -H "Consumer-Token: ${SDKMAN_CONSUMER_TOKEN}" \
    -H "Content-Type: application/json" \
    -H "Accept: application/json" \
    -d '{"candidate": "mulefd", "version": "'${mulefd_version}'", "url": "https://github.com/manikmagar/mulefd/releases/download/v'${mulefd_version}'/mulefd-'${mulefd_version}'.zip"}' \
    https://vendors.sdkman.io/release

## Set existing Version as Default for Candidate

echo "Set default version on sdkman"
curl -X PUT \
    -H "Consumer-Key: ${SDKMAN_CONSUMER_KEY}" \
    -H "Consumer-Token: ${SDKMAN_CONSUMER_TOKEN}" \
    -H "Content-Type: application/json" \
    -H "Accept: application/json" \
    -d '{"candidate": "mulefd", "version": "'${mulefd_version}'"}' \
    https://vendors.sdkman.io/default

RELURL=`curl -i https://git.io -F url=https://github.com/manikmagar/mulefd/releases/tag/v${mulefd_version} | grep Location | sed -e 's/Location: //g' | tr -d '\n' | tr -d '\r'`
echo git.io = [$RELURL]

## Broadcast message with pointer to change log
curl --trace-ascii curl.trace -X POST \
    -H "Consumer-Key: ${SDKMAN_CONSUMER_KEY}" \
    -H "Consumer-Token: ${SDKMAN_CONSUMER_TOKEN}" \
    -H "Content-Type: application/json" \
    -H "Accept: application/json" \
    -d '{"candidate": "mulefd", "version": "'${mulefd_version}'", "url": "'${RELURL}'"}' \
    https://vendors.sdkman.io/announce/struct