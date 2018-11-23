package irc.tula.tg.core.plugin;

import irc.tula.tg.core.ChannelBot;
import irc.tula.tg.core.Nickname;

public interface Plugin {
    String getName();
    void initialize(ChannelBot bot);
    boolean process(ChannelBot bot, Long chatId, Nickname nickName, String text);
    void release(ChannelBot bot);
}
