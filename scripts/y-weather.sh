#!/bin/bash

HBIN=/usr/bin/google-chrome-stable
WS=1280,1280

URL="https://yandex.ru/pogoda/tula?lat=54.184103&lon=37.611658"

DIR="/home/ec2-user/bin/bots/tmp"
TG="y-weather.png"

cd "$DIR"

# cache 5 min
TTL=`perl -e 'printf "%d\n" ,(time()-((stat(shift))[9]))/60;' ~/bin/bots/tmp/y-weather.png`
#echo "TTL: $TTL"
if [[ $TTL < 5 ]] ; then
    echo "$(pwd)/$TG"
    exit
fi

rm -f screenshot.png y-weather.png

$HBIN --headless --screenshot \
    --window-size=$WS --default-background-color=0 \
    "$URL"

convert -crop 590x288+27+157 screenshot.png $TG
echo "$(pwd)/$TG"
