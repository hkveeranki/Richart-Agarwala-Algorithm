TARGET=Node.class
CC=javac
FLAGS= -d bin -cp ".:lib/protobuf.jar" 
all: PROTO $(TARGET)
PROTO: Message.proto
	protoc Message.proto --java_out=src
$(TARGET): src/Node.java src/VectorClock.java src/Nodedef.java 
	$(CC) $(FLAGS) src/assignment/MessageProto.java
	
clean:
	rm bin/*.class
