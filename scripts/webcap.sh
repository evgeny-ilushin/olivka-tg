#!/bin/bash

HBIN=/usr/bin/google-chrome-stable
WS=1280,1280
YCROP=590x288+27+157
FCROP=590x288+228+145

DURL="https://yandex.ru/pogoda/tula?lat=54.184103&lon=37.611658"
#URL="https://yandex.ru/pogoda/tshekino?"
URL="$1"; shift
CROP="$1"; shift
PAMS="$@"

DIR="/projects/olivka/telegram-2018/scripts"
#DIR="/home/ec2-user/bin/bots/tmp"

if [ "_$URL" == "_" ]; then
    URL="$DURL"
fi

cd "$DIR"


PX=$(echo -n "$URL" | md5sum | tr -d ' ' | tr -d '-')

echo "[URL : $URL]" > /dev/stderr
echo "[MD5: $PX]" > /dev/stderr
echo "[WS : $WS]" > /dev/stderr
echo "[CROP : $CROP]" > /dev/stderr
echo "[PAMS: $PAMS]" > /dev/stderr

TG="webcap_$PX.png"


# cache 5 min
TTL=`perl -e 'printf "%d\n" ,(time()-((stat(shift))[9]))/60;' $TG`
echo "[TTL: $TTL]" > /dev/stderr
if [[ $TTL -le 5 ]] ; then
    echo "$(pwd)/$TG"
    exit
fi

rm -f screenshot.png "$TG"
#y-weather.png

$HBIN --headless --screenshot \
    --window-size=$WS --default-background-color=0 \
    "$URL"

convert -crop $CROP screenshot.png $TG

echo "$(pwd)/$TG"

