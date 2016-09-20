#!/bin/sh
# Creating Output folder
mkdir -p bin
# Compiling
make
# Running
cd bin
killall rmiregistry
killall rmiregistry
killall rmiregistry
rmiregistry &
sleep 2
for i in `seq 0 $(($2-1))`
do
java  Node -i $i -n $2 & 
done
