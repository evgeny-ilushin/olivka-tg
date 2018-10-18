package irc.tula.tg.core;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StandaloneBot extends BotCore {
    private static final String DEFAULT_TOKEN = "294103149:AAGPawepBdjAtu9z9aKDj2rLwwdNt0UDi9E";

    public StandaloneBot(String token) {
        super(token);
    }

    public static void main(String[] args) {
        BotCore bot = new BotCore(DEFAULT_TOKEN);

        log.info("Starting bot...");
        bot.start();
    }

}
