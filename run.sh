#!/usr/bin/env bash

java --enable-preview --add-modules jdk.incubator.concurrent -jar target/$(basename target/app-java21-template-*.jar)