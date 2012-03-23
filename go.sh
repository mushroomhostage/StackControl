#!/bin/sh -x
CLASSPATH=../craftbukkit-1.1-R8.jar javac *.java -Xlint:unchecked -Xlint:deprecation
rm -rf me
mkdir -p me/exphc/StackControl
mv *.class me/exphc/StackControl
jar cf StackControl.jar me/ *.yml *.java
