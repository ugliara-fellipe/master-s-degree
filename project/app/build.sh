#!/bin/bash

PROJECT=$( cd $( dirname ${0} ) && cd .. && pwd )

cd ${PROJECT}

java -cp "${PROJECT}/jar/saci.jar" saci.Saci "${PROJECT}/app/${1}/proj.pyan" -cyanlang "${PROJECT}/lib" -javalib "${PROJECT}/lib/javalib" -nojavac

javac -cp "${PROJECT}/jar/treplica-0.1.0.jar" -sourcepath "${PROJECT}/lib/java-for-cyan_lang":"${PROJECT}/lib/javalib":"${PROJECT}/app/java-for-${1}" ${PROJECT}/app/java-for-${1}/main/*.java

