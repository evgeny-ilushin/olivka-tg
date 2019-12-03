#!/bin/bash

LOG=~/bin/bots/log/respawnd.log
BIN=~/bin/bots/olivka-tg/run_in_bg

# autobg
if [ -z "$@" ]; then
    eval "./$0 in BG" &>/dev/null & disown; exit
fi

function PIDOF {
    ps -fA | grep "java" | grep "olivka-tg" | awk '{print $2}'
}


while true; do

    if [ -z "$(PIDOF)" ]; then
	echo "Not running: $(date)" | tee -a $LOG
	~/bin/bots/olivka-tg/run_in_bg
#    else
#	echo "on" | tee -a $LOG
    fi
    sleep 10
done
