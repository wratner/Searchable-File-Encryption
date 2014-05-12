#!/bin/bash
#Takes as 
#   argument 1: a directory consisting of small email directory in MP3 for input
#   argument 2: a file consisting the encryption key
#Generates an output consisting of 
#   argument 3: index stored by the client on the phone as well
#   argument 4 : (possibly a directory or a single tarred file) encrypted files to to be stored on the server 

#NOTE:  Your index generation should be in Java
#Call your java code with the arguments here

mkdir small_output
echo "Time to place"
time java -cp `pwd`/bin mp3.Main ../small illinois 1 blah small_output blah

echo "time to index"
time java -cp `pwd`/bin mp3.Main ./small_output illinois 1 indexOnly small_output blah

echo "Time to encrypt"
time java -cp `pwd`/bin mp3.Main ../small illinois 1 blah small_output encrypt

