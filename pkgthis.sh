#!/bin/bash
    mvn deploy:deploy-file -DgroupId=org.ipfs \
      -DartifactId=api \
      -Dversion=0.1 \
      -Dpackaging=jar \
      -Dfile=target/api-0.0.1-SNAPSHOT.jar \
      -DrepositoryId=repo \
      -Durl=file:///tmp/
