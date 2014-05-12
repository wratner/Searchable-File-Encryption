#!/bin/bash

#rsync --update -a -v -r --delete -e ssh ../Searchable-File-Encryption spraber2@172.22.152.61:~
rsync --update -a -v -r -e ssh ../Searchable-File-Encryption spraber2@172.22.152.61:~
