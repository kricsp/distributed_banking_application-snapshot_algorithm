#!/bin/bash +vx
LIB_PATH=$"/home/suri/Desktop/Github/distributed_banking_application-snapshot_algorithm/Distributed Banking Application -  Global Snapshot/lib/protobuf-java-3.4.0.jar"
#port
java -classpath bin/classes:$LIB_PATH Controller $1 $2
