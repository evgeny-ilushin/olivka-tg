package irc.tula.tg.core;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

@Deprecated
public abstract class NewWorld {
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private static String  locale = "utf-8";

    public static final String NICK_PREFIX = "@";
}
