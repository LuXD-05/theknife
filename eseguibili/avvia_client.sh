#!/bin/bash
cd "$(dirname "$0")/.."

mvn -pl theknife-common -am install
if [ $? -ne 0 ]; then
    echo "Build di theknife-common fallita!"
    exit 1
fi

cd theknife-frontend
mvn org.openjfx:javafx-maven-plugin:0.0.8:run