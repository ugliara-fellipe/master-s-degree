#!/bin/bash

PROJECT=$( cd $( dirname ${0} ) && cd .. && pwd )

mkdir ${PROJECT}/temp
cd ${PROJECT}/temp
jar xf ${PROJECT}/jar/saci.jar

if [ -d ${PROJECT}/lib/cyan/lang/--meta ]; then
  rm -r ${PROJECT}/lib/cyan/lang/--meta
fi

mkdir ${PROJECT}/lib/cyan/lang/--meta
cp -a meta/. ${PROJECT}/lib/cyan/lang/--meta/meta


if [ -d ${PROJECT}/lib/treplica/--meta ]; then
  rm -r ${PROJECT}/lib/treplica/--meta
fi
mkdir ${PROJECT}/lib/treplica/--meta
mkdir ${PROJECT}/lib/treplica/--meta/meta
mv ${PROJECT}/lib/cyan/lang/--meta/meta/treplica ${PROJECT}/lib/treplica/--meta/meta

rm -r ${PROJECT}/temp
rm -r ${PROJECT}/lib/cyan/lang/--meta/meta/cyanLang/CyanMetaobjectTreplica*

