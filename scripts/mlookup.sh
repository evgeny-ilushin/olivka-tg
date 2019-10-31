#!/bin/bash

#MDN=14082257140
#MDN=79028472038
MDN=$@

#DB=/home/ec2-user/bin/bots/data/mcclookup.csv
#T=dev1
#KEY=~/nm-dev.pem

DB=/home/zloy/Documents/nm/mcclookup.csv
T=52.44.33.68
KEY=/home/zloy/Downloads/nm-dev.pem


if [ "$MDN" == "" ]; then
    echo "invalid phone number"
    exit 0
fi

if [[ ! $MDN =~ ^[0-9+]+$ ]]
then
    echo "invalid character in phone number"
    exit 1
else
    MDN=$(echo $MDN | tr -d '+')
fi


LO=$(ssh -C -i $KEY ec2-user@$T /home/ec2-user/bin/NumberLookup/mlookup.sh $MDN)
OK=$(echo $LO | grep "result=OK;imsi")

if [ "$OK" == "" ]; then
    echo "not found"
    exit 0
fi

IMSI=$(echo "$LO" | cut -d "=" -f 3)


INFO=$(cat $DB | grep $IMSI | cut -d '|' -f 2)


if [ "$INFO" == "" ]; then
    echo "unknown IMSI: $IMSI"
    exit 0
fi

echo $INFO

