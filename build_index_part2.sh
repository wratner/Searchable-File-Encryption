#!/bin/bash
#Takes as 
#   argument 1: a directory consisting of large email directory in MP3 for input
#   argument 2: a file consisting the encryption key
#Generates an output consisting of 
#   argument 3: (cached partial) index stored by the client on the phone as well as
#   argument 4 : (possibly a directory or a single tarred file) encrypted files to to be stored on the server 

#NOTE:  Your index generation should be in Java.  This should follow the SSE.indexgen described in Figure 8 of paper
#Call your java code with the arguments here

#java -cp `pwd`/bin mp3.Main ../large illinois 2
#mkdir large_output
#echo "Time to place"
#time java -cp `pwd`/bin mp3.Main ../large illinois 2 blah large_output blah

#echo "time to index"
#time java -Xmx1024m -cp `pwd`/bin mp3.Main ./large_output illinois 2 indexOnly large_output blah

#echo "Time to encrypt"
#time java -cp `pwd`/bin mp3.Main ../small illinois 2 blah small_output encrypt
#mkdir small_output

rm -rf small_output2
mkdir -p small_output2/keywords

echo "Time to place"
time java -cp `pwd`/bin mp3.Main ../small illinois 2 blah small_output2 blah

echo "time to index"
time java -Xmx1024m -cp `pwd`/bin mp3.Main ./small_output2 illinois 2 indexOnly small_output2 blah

echo "Time to encrypt"
#time java -cp `pwd`/bin mp3.Main ../small illinois 2 blah small_output2 encrypt
time java -cp `pwd`/bin mp3.CreateBlindStore small_output2 illinois

