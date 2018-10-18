package irc.tula.tg.core;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Deprecated
public abstract class Cave {
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private static String  locale = "windows-1251";

    public static final String LINE_SEPARATOR = "|";
    public static final int DEFAULT_REPLY_DELAY = 0; // No delay

    public static final String RDB_ROOT = "/home/olivka/bot/dat";
}
