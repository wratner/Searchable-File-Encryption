#!/bin/bash
#make any changes need to run the server (including passing the arguments to the encrypted storage location or any other parameters)

if [[ `uname` == 'Linux' ]]; then
  java -cp `pwd`/bin BlindStorageServer `ifconfig eth0 | grep "inet addr" | awk 'sub(/addr:/,""){print $2}'` 
else 
  java -cp `pwd`/bin BlindStorageServer `ifconfig en1 | grep "netmask" | awk 'sub(/inet/,""){print $1}'` 
fi
