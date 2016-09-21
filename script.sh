#!/bin/bash
# Creating Output folders
if [ $# -ne 4 ]
then
echo 'Invalid Arguments'
echo 'Usage: ./script.sh -n <number_of_nodes> -o <output_file>'
exit -1
fi
echo "Setting Up files...."
mkdir -p bin
rm *.out
# Compiling
echo "Compiling...."
make
# Running
echo "Starting the Instances...."
cd bin
touch "$4"
rm $4
touch "$4"
killall rmiregistry
sleep 1
rmiregistry &
sleep 1
for i in `seq 0 $(($2-1))`
do
java  -cp ".:../lib/protobuf.jar" Node -i $i -n $2 -o $4> ../Node-$i.out &
done
echo "All Nodes Started.. Waiting for Ctrl+C"
trap 'killall java;killall rmiregistry' INT
wait
