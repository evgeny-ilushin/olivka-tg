#!/bin/bash

LOG=/tmp/.calc.log

BC=" ~/.bcrc -l"

TEXT="$@"
RES="OK"

SCALE=2

#    echo "$TEXT" | bc
#    echo "180/3.1416*asin(1/sqrt(3))" | bc ~/.bcrc -l
#exit

function FixZeros {
    #echo "FixZeros: $I"
    local I="$@"
    if ! [[ "$I" =~ [^a-zA-Z\ ] ]]; then
	echo "$I" | tr '[:upper:]' '[:lower:]'
    else
	echo "$I" | awk '{printf "%08f\n", $0}' | sed '/\./ s/\.\{0,1\}0\{1,\}$//'
    fi
}

echo -e "[$TEXT]" >> $LOG

if [[ $TEXT == *"."* ]] || [[ $TEXT == *","* ]]; then
    RES=$(echo "$TEXT" | bc ~/.bcrc -l 2>&1 | head -n 1)
#echo 1
#    echo "$TEXT" | bc ~/.bcrc -l
else
    RES=$(echo "scale=$SCALE; $TEXT" | bc ~/.bcrc -l 2>&1 | head -n 1)
#echo 2
#    echo "scale=$SCALE; $TEXT" | bc ~/.bcrc -l
fi


#echo -e "################1: $RES"

# WTF
#if [[ $RES == *":"* ]]; then
#echo "::::::"
#    RES=$(echo "$RES" | cut -d':' -f 2 | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')
#else
#    RES=$(echo "scale=$SCALE; $TEXT" | bc ~/.bcrc -l 2>&1 | head -n 1)
#fi

#echo -e "################2: $RES"

if [ -z "$RES" ]; then
    echo "syntax error"
else
    FixZeros "$RES"
fi

echo -e "[$RES]" >> $LOG

