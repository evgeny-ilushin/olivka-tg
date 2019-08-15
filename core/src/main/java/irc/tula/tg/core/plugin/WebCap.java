package irc.tula.tg.core.plugin;

import irc.tula.tg.core.ChannelBot;
import irc.tula.tg.core.NewWorld;
import irc.tula.tg.core.entity.IncomingMessage;
import irc.tula.tg.util.ExecCommand;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class WebCap implements Plugin {

    private static final String NAME = "webcap";
    public static final String[] ALIASES = { "wcap", "wc" };

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
        String img = callWcScript(bot, msg, "webcap", params);
        if (img != null) {
            log.info("Script response: {}", img);
            bot.sendImageToChat(msg.getChatId(), img);
            return true;
        } else {
            log.error("Zero reply from script");
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

    private String callWcScript(ChannelBot bot, IncomingMessage msg, String scriptName, String[] params) {
        log.info("callScript: {} {}", msg, scriptName);
        String sres = null;

        String binary = bot.getConfig().getScriptDir(scriptName + NewWorld.SCRIPT_SUFFIX);
        try {
            if (!Files.exists(Paths.get(binary))) {
                log.error("WcScript not found: {}", binary);
                return sres;
            }

            ArrayList<String> cp = new ArrayList<>();
            cp.add(binary);
            cp.addAll(Arrays.asList(params));
            String[] args = cp.toArray(new String[cp.size()]);
            ExecCommand ec = params == null? new ExecCommand(binary) :
                new ExecCommand(cp.toArray(new String[cp.size()]));
            Thread.sleep(100);
            //ec.getOutput();
            String res = ec.output;
            if (StringUtils.isNotBlank(res)) {
                sres = res;
            } else {
                log.error("callScript zero reply");
                Thread.sleep(500);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return sres;
    }
}
