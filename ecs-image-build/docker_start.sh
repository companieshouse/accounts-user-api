#!/bin/bash
#
# Start script for accounts-user-service

PORT=8080

exec java -jar -Dserver.port="${PORT}" "accounts-user-api.jar"