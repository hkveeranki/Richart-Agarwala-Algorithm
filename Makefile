TARGET=Node.class
CC=javac
all: $(TARGET)
$(TARGET): src/Node.java src/VectorClock.java src/Nodedef.java
	javac -d bin src/*.java
clean:
	rm bin/*.class
