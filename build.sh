#!/usr/bin/bash

./gradlew shadowJar && \
cp build/libs/$(ls build/libs | grep jar) /home/xap3y/Desktop/1.20.4/plugins && \
cp build/libs/$(ls build/libs | grep jar) /home/xap3y/Desktop/four/plugins