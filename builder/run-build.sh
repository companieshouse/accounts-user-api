#!/bin/bash

source "$HOME/.sdkman/bin/sdkman-init.sh"
sdk default java 21.0.2-graalce
mvn -Dmaven.test.skip package

echo Starting JAR with the native-image-agent...
MONGODB_DATABASE=account MONGODB_URL=mongodb://host.docker.internal \
    java -agentlib:native-image-agent=config-output-dir=src/main/resources/META-INF/native-image \
    -Dmanagement.health.mongo.enabled=false \
    -jar target/accounts-user-api-unversioned.jar &
pid=$!
until [ "$(curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/accounts-user-api/healthcheck)" == "200" ]; do
    sleep 5
done
kill $pid

mvn -Dmaven.test.skip -Pnative package
mv target/accounts-user-api target/app