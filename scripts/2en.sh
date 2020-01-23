#!/bin/bash

LOG=/tmp/2EN.log
TEXT="$@"

echo "$(date) IN: $TEXT" >> $LOG

#TEXT="$(echo $@ | iconv -f 'windows-1251' -t 'utf-8')"

echo "UTF: $TEXT" >> $LOG


node /home/ec2-user/bin/bots/olivka-tg/scripts/2en.js "$TEXT" | tee -a $LOG

