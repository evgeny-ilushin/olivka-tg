#!/bin/bash

LOG=/tmp/2EN.log
TEXT="$@"

URL=https://newstula.ru/newfeed/vsenovosti
ITEM="/rss/channel/item/title="
EITEM="\/rss\/channel\/item\/title="

curl "$URL" | xml2 | grep "$ITEM" | shuf -n 1 | sed "s/$EITEM//g"

