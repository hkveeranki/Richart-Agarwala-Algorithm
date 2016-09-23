#!/bin/bash
if [ $# -ne 4 ]
then
echo 'Invalid Arguments'
echo 'Usage: ./script.sh -n <number_of_nodes> -o <output_file>'
exit -1
fi
# Creating Output folders and removing old ones
echo "Setting Up files...."
mkdir -p bin
rm -f *.out
# Compiling
echo "Compiling...."
make
# Running
cd bin
touch "$4"
rm $4
touch "$4"
echo "Killall all rmiregistry..."
killall  rmiregistry 2>/dev/null
echo "Done"
sleep 1
echo "Starting RMI Registry..."
rmiregistry &
echo "Done"
sleep 1
echo "Starting the Instances...."
for i in `seq 0 $(($2-1))`
do
java  -cp ".:../lib/protobuf.jar" Node -i $i -n $2 -o $4> ../Node-$i.out &
done
echo "All Nodes Started.. Waiting for Ctrl+C"
trap 'killall java;killall rmiregistry' INT
wait
