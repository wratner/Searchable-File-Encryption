#!/bin/bash

#make any changes needed to compile the server
#javac src/mp3/BlindStorageServer.java
#javac src/mp3/BlindStorage.java -cp src
#javac -d bin src/mp3/*.java
#javac -d bin -cp `pwd`/bin src/BlindStorageServer.java
mkdir bin
javac -d bin src/mp3/*.java
javac -d bin -cp `pwd`/bin src/BlindStorageServer.java
