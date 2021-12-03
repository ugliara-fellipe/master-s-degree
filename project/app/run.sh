#!/bin/bash

PROJECT=$( cd $( dirname ${0} ) && cd .. && pwd )

rm -rf /var/tmp/magic${2}
mkdir /var/tmp/magic${2}

java -cp "${PROJECT}/jar/treplica-0.1.0.jar":"${PROJECT}/jar/slf4j-api-1.7.18.jar":"${PROJECT}/jar/slf4j-jdk14-1.7.18.jar":"${PROJECT}/lib/java-for-cyan_lang":"${PROJECT}/lib/javalib":"${PROJECT}/app/java-for-${1}" main.Proj -args ${2} ${3}

