#!/bin/bash

SRC="/projects/olivka/olivka-tg/bot/data-windows-1251"
DST="/projects/olivka/olivka-tg/bot/data"

ls -1 "$SRC" | grep ".rdb$\|.ini$\|.db$" | while read l; do
    echo "Converting $l..."
    cat "$SRC/$l" | iconv -f "windows-1251" -t "UTF-8" > "$DST/$l"
done

#cat "$SRC/$l" | iconv -f "windows-1251" -t "UTF-8" > "$DST/$l"





