package irc.tula.tg.core;

import irc.tula.tg.core.entity.Nickname;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Arrays;

@Slf4j
public class StandaloneBotTest {
    @Test
    public void testChanServ() {
        BotConfig c = getTestConfig();
        StandaloneBot bot = new StandaloneBot(c);
        log.info("Starting bot using {} ...", c);

        boolean csRes = bot.chanserv(-100108239087L, new Nickname(44L, "zloy", true), "а кто в жопе");
        csRes = bot.chanserv(-1001082390874L, new Nickname(10L, "zloy", true), "глоток");
    }

    public static BotConfig getTestConfig() {
        return new BotConfig("../bot",
                "123123123:454564",
                Cave.getEncoding(),
                Arrays.asList("@murzambek", "@zloy"),
                Arrays.asList("bot", "MyBot", "@my_bot_nick"),
                Arrays.asList("123"),
                true,
                false,
                true);
    }
}
