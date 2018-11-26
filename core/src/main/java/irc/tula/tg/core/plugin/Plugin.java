package irc.tula.tg.core.plugin;

import irc.tula.tg.core.ChannelBot;
import irc.tula.tg.core.Nickname;

import java.util.List;

public interface Plugin {
    String getName();
    List<String> getNames();
    void initialize(ChannelBot bot);
    boolean process(ChannelBot bot, Long chatId, Nickname nickName, String text, String pluginName);
    void release(ChannelBot bot);
}
