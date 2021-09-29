package irc.tula.tg.core;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
/*
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
*/
import irc.tula.tg.core.data.JsonObjectMapper;
import irc.tula.tg.core.data.MyObjectMapper;
import irc.tula.tg.core.entity.IncomingMessage;
import irc.tula.tg.core.entity.Nickname;
import irc.tula.tg.core.plugin.*;
import irc.tula.tg.util.ExecCommand;
import irc.tula.tg.util.TextLog;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Slf4j
public class StandaloneBot { //extends BotCore implements ChannelBot, LambdaBot {

    // olivka
    //private static final String DEFAULT_TOKEN = "294103149:AAGPawepBdjAtu9z9aKDj2rLwwdNt0UDi9E";
    //private static final String[] BOT_NAMES = { "olivka", "оливка", "@OlivkaIrcBot" };

    // rotten
    //private static final String DEFAULT_TOKEN = "668163913:AAE98c1hN0O5m1kyE3e9XgBLLQolN96fpH4";
    //private static final String[] BOT_NAMES = { "rotten", "гнилой", "@rottenbot2018_bot" };

    //private static final String BOT_HOME = "/home/ec2-user/bin/bots/data";


    public StandaloneBot() {
    }

    /*

    public StandaloneBot(BotConfig config) {
        super(config);
        callbacks = new TextLog(getConfig().getLogDir("updates.log"));
        mapper = new JsonObjectMapper(getConfig().getDataDirName());
        loadState();
        loadPlugins();
        loadCore();
    }

    public static void main(String[] args) {
        try {
            if (args.length != 1) {
                System.out.println("Usage: BOT_JAR <path to config.json>");

                // Sample
                BotConfig c = BotConfig.getSample();
                String sampleConfig = JsonObjectMapper.dumpConfig(c);
                System.out.println("Sample config:\n\t" + sampleConfig);
                return;
            }

            String cfgPath = args[0];

            if(!Files.exists(Paths.get(cfgPath))) {
                throw new FileNotFoundException(cfgPath);
            }

            Optional<BotConfig> c = JsonObjectMapper.readConfig(cfgPath);
            if (c.isPresent()) {
                StandaloneBot bot = new StandaloneBot(c.get());
                log.info("Starting bot using {} ...", cfgPath);

                if (bot.getConfig().isRunTests()) {
                    consoleSession(bot);
                    //my_tests(bot);
                }
                else {
                    bot.start(bot);
                }
            }
        } catch (Exception ex) {
            log.error("Bot crashed (config: {}): {}", args[0], ex);
            ex.printStackTrace();
        }
    }

    */

}
