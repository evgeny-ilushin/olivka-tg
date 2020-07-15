package irc.tula.tg.core;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public abstract class NewWorld {
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private static String encoding = "utf-8";

    public static final String NICK_PREFIX = "@";
    public static final String PATH_SEPARATOR = "/";
    public static final String EOL = "\n";
    public static final String LINE_SEPARATOR = "\n";

    public static final String SCRIPT_SUFFIX = ".sh";
    public static final String NICK_SEPARATOR = ", ";
}
