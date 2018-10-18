package irc.tula.tg.core;

public interface BotConfig {
    default BotConfig getDefault() {
        return new DefaultBotConfig();
    }

    static BotConfig getDefaultConfig() {
        return new DefaultBotConfig();
    }

    String getLogDir();
}

class DefaultBotConfig implements BotConfig {
    @Override
    public String getLogDir() {
        return "";
    }
}