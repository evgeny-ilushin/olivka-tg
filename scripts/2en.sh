#!/bin/bash

LOG=/tmp/2EN.log
TEXT="$@"

echo "$(date) IN: $TEXT" >> $LOG

#TEXT="$(echo $@ | iconv -f 'windows-1251' -t 'utf-8')"

echo "UTF: $TEXT" >> $LOG


node /projects/linux/google-translate/2en.js "$TEXT" | tee -a $LOG

