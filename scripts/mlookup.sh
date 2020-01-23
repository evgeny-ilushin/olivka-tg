#!/bin/bash

cd ~/bin/bots/olivka-tg/scripts

./mlookup-bin.sh $@ 2>&1 | tee -a mlookup.log