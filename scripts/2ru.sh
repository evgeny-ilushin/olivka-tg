#!/bin/bash

LOG=/tmp/2RU.log
TEXT="$@"

echo "$(date) IN: $TEXT" >> $LOG

#TEXT="$(echo $@ | iconv -f 'windows-1251' -t 'utf-8')"

echo "UTF: $TEXT" >> $LOG


R="$(node /home/ec2-user/bin/bots/olivka-tg/scripts/2ru.js $TEXT)"

#R="$(echo $R | iconv -t 'windows-1251' -f 'utf-8')"


echo "$R" | tee -a $LOG



