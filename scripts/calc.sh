#!/bin/bash

LOG=/tmp/.calc.log

TEXT="$@"
RES="OK"

echo -e "[$TEXT]" >> $LOG

if [[ $TEXT == *"."* ]] || [[ $TEXT == *","* ]]; then
    RES=$(echo "$TEXT" | bc 2>&1 | head -n 1)
else
    RES=$(echo "scale=2; $TEXT" | bc 2>&1 | head -n 1)
fi

if [[ $RES == *":"* ]]; then
    RES=$(echo "$RES" | cut -d':' -f 2 | sed -e 's/^[[:space:]]*//' -e 's/[[:space:]]*$//')
else
    RES=$(echo "scale=2; $TEXT" | bc 2>&1 | head -n 1)
fi

if [ -z "$RES" ]; then
    echo "syntax error"
else
    echo "$RES"
fi

echo -e "[$RES]" >> $LOG

