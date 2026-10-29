#!/bin/sh
set -eu

: "${BACKEND_ORIGIN:=http://backend:8081}"
export BACKEND_ORIGIN
envsubst '${BACKEND_ORIGIN}' \
  < /etc/nginx/nginx.conf.template \
  > /etc/nginx/conf.d/default.conf
