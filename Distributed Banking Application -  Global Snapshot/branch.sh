#!/bin/bash +vx
LIB_PATH=$"/home/suri/Desktop/DBA/lib/protobuf-java-3.4.0.jar"
#port
java -classpath bin/classes:$LIB_PATH Branch $1 $2
