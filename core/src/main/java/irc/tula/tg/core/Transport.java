package irc.tula.tg.core;

public interface Transport {
    void init(BotConfig config);

    BotConfig getBotConfig();
}
