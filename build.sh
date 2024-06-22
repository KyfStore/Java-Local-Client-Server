#!/bin/bash

# Delete all .class files
echo "Deleting existing .class files..."
find . -name "*.class" -type f -delete

# Compile all .java files
echo "Compiling .java files..."
javac *.java

# Check if compilation was successful
if [ $? -eq 0 ]; then
    echo "Compilation successful."

    # Run the Server class
    java Server
else
    echo "Compilation failed."
fi
