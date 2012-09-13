#!/bin/bash
if [ $# -ne 1 ]; then
    echo "Mööööp"
else 
    mvn exec:exec -DgeneratorTarget="$1"
fi 


