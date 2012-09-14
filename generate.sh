#!/bin/bash
if [ $# -ne 1 ]; then
    echo "Usage: generate.sh /target/folder"
else 
    mvn clean package exec:exec -DgeneratorTarget="$1"
fi 


