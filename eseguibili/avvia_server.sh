#!/bin/bash
cd "$(dirname "$0")/.."
mvn -pl theknife-backend -am io.quarkus.platform:quarkus-maven-plugin:3.20.0:dev