#!/bin/bash
    mvn deploy:deploy-file -DgroupId=net.iowntheinter \
      -DartifactId=ipfs \
      -Dversion=0.1 \
      -Dpackaging=jar \
      -Dfile=target/api-0.0.1-SNAPSHOT.jar \
      -DrepositoryId=my-repo \
      -Durl=file:///tmp/
