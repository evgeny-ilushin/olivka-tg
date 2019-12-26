#!/bin/bash

LOG=/tmp/2UA.log
TEXT="$@"
#TEXT="$(echo $@ | iconv -f 'windows-1251' -t 'utf-8')"

echo "$(date) IN: $TEXT" >> $LOG

#TEXT="$(echo $@ | iconv -f 'windows-1251' -t 'utf-8')"

echo "UTF: $TEXT" >> $LOG


R="$(node 2ua.js $TEXT)"

if [[ "$R" =~ ^\(uk\).* ]]; then
    R="$(node 2uar.js $TEXT)"
fi;

#R="$(echo $R | iconv -t 'windows-1251' -f 'utf-8')"


echo "$R" | tee -a $LOG



