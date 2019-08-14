package irc.tula.tg.core.plugin;

import irc.tula.tg.core.ChannelBot;
import irc.tula.tg.core.entity.IncomingMessage;

import java.util.List;

public interface Plugin {
    String getName();
    List<String> getNames();
    void initialize(ChannelBot bot);
    boolean process(ChannelBot bot, IncomingMessage msg, String pluginName, String params);
    boolean processCommand(ChannelBot bot, String cmd, String params, IncomingMessage msg, String pluginName);
    void release(ChannelBot bot);
}
