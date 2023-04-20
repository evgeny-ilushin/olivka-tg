package irc.tula.tg.plugin;

import com.pengrad.telegrambot.model.Message;
import irc.tula.tg.ChannelBot;
import irc.tula.tg.entity.IncomingMessage;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Slf4j
public class Sticker implements Plugin {

    private static final String NAME = "sticker";
    public static final String[] ALIASES = { "sti", "pic" };

    private static final String DONNO_RDB = "donno";

    private boolean loaded = false;

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public List<String> getNames() {
        return Arrays.asList(ALIASES);
    }

    @Override
    public void initialize(ChannelBot bot) {
        if (!loaded) {
            loaded = true;
        }
    }

    @Override
    public boolean process(ChannelBot bot, IncomingMessage msg, String pluginName, String[] params) {
        bot.typeOnChannel(msg.getChatId());
        if (sendSticker(bot, msg, params) != null) {
            return true;
        } else {
            log.error("Zero reply from API");
            bot.answerRdb(msg, DONNO_RDB);
        }
        return false;
    }

    @Override
    public boolean processCommand(ChannelBot bot, String cmd, String params, IncomingMessage msg, String pluginName) {
        return false;
    }

    @Override
    public void release(ChannelBot bot) {

    }

    private String sendSticker(ChannelBot bot, IncomingMessage msg, String[] params) {
        log.info("sendSticker: {} {}", msg, params);
        String sres = null;

        try {
            String sti = params[0].trim();
            Optional<Message> res = bot.sendSticker(msg.getChatId(), sti);
            sres = "" + (res.isPresent()? res.get().messageId() : res);
            log.info("TG.sendSticker: {}", res);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return sres;
    }
}
