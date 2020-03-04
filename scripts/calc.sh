#!/bin/bash

LOG=/tmp/.calc.log

TEXT="$@"
RES="OK"

SCALE=2

function FixZeros {
    echo "$@" | awk '{printf "%08f\n", $0}' | sed '/\./ s/\.\{0,1\}0\{1,\}$//'
#    echo "$@" | sed '/\./ s/\.\{0,1\}0\{1,\}$//'
}

echo -e "[$TEXT]" >> $LOG

if [[ $TEXT == *"."* ]] || [[ $TEXT == *","* ]]; then
    RES=$(echo "$TEXT" | bc 2>&1 | head -n 1)
else
    RES=$(echo "scale=$SCALE; $TEXT" | bc 2>&1 | head -n 1)
fi

if [[ $RES == *":"* ]]; then
    RES=$(echo "$RES" | cut -d':' -f 2 | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')
else
    RES=$(echo "scale=$SCALE; $TEXT" | bc 2>&1 | head -n 1)
fi

if [ -z "$RES" ]; then
    echo "syntax error"
else
    FixZeros "$RES"
fi

echo -e "[$RES]" >> $LOG

