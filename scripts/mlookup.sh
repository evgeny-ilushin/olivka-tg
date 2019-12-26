#!/bin/bash

/projects/olivka/telegram-2018/olivka-tg/scripts/mlookup-bin.sh $@ 2>&1 | tee -a /tmp/lookup.log

