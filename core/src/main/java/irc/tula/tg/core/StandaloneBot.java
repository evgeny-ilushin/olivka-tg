package irc.tula.tg.core;

import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import irc.tula.tg.core.data.JsonObjectMapper;
import irc.tula.tg.core.data.MyObjectMapper;
import irc.tula.tg.core.entity.IncomingMessage;
import irc.tula.tg.core.entity.Nickname;
import irc.tula.tg.core.plugin.Plugin;
import irc.tula.tg.core.plugin.SoWhat;
import irc.tula.tg.util.ExecCommand;
import irc.tula.tg.util.TextLog;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Slf4j
public class StandaloneBot extends BotCore implements UpdatesListener, ChannelBot {

    // olivka
    //private static final String DEFAULT_TOKEN = "294103149:AAGPawepBdjAtu9z9aKDj2rLwwdNt0UDi9E";
    //private static final String[] BOT_NAMES = { "olivka", "оливка", "@OlivkaIrcBot" };

    // rotten
    //private static final String DEFAULT_TOKEN = "668163913:AAE98c1hN0O5m1kyE3e9XgBLLQolN96fpH4";
    //private static final String[] BOT_NAMES = { "rotten", "гнилой", "@rottenbot2018_bot" };

    //private static final String BOT_HOME = "/home/ec2-user/bin/bots/data";

    private static final String INFO2 = "info2.db";
    private static final String DONNO_RDB = "donno";

    private static final String MEMBERS_CACHE = "members.json";

    public BotConfig getConfig() { return config; }

    @Getter
    private TextLog callbacks;

    // Members
    @Getter
    private HashSet<Nickname> members = new HashSet<>();

    // Info2
    @Getter
    Info2Resource info2 = new Info2Resource(getConfig().getDataDirName(), INFO2);

    // RDBs
    @Getter
    private HashMap<String, RDBResource> rdbStore = new HashMap<>();

    // DBs
    @Getter
    private final MyObjectMapper mapper;

    // Pluigins
    HashMap<String, Plugin> plugins = new HashMap<>();

    public StandaloneBot(BotConfig config) {
        super(config);
        callbacks = new TextLog(getConfig().getLogDir("updates.log"));
        mapper = new JsonObjectMapper(getConfig().getDataDirName());
        loadState();
        loadPlugins();
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

                if (bot.getConfig().isDebug()) {
                    my_tests(bot);
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

    private void addPlugin(Plugin plugin) {
        if (plugin.getNames() != null) {
            plugin.getNames().forEach(i -> plugins.put(i, plugin));
        }
        plugins.put(plugin.getName(), plugin);
    }

    private void loadPlugins() {
        addPlugin(new SoWhat());
        plugins.forEach((k,v) -> v.initialize(this));
    }

    protected void onUpdate(Update update) {
        callbacks.add(toJson(update));

        if (update.message() != null) {
            Message m = update.message();
            User from = m.from();
            String nickName = from.username();
            Nickname nick;

            if (nickName == null) {
                nickName = from.firstName();
                nick = new Nickname(nickName, false);
            } else {
                nick = new Nickname(nickName, true);
            }

            // Ignore other bots?
            if (from.isBot()) {
            }

            // Chat only
            if (m.chat() != null && m.chat().title() != null) {
                Chat c = m.chat();
                Long replyChatId = c.id();

                // I am text only
                if (m.text() != null) {
                    chanserv(replyChatId, nick, m.text());
                }
            }
        }
    }

    private void processUpdate(Update update) {
        try {
            log.info("update: {}", update);
            onUpdate(update);
        } catch (Exception ex) {
            log.error("processUpdate: {}", ex);
        }
    }

    public int process(List<Update> list) {
        list.forEach(item->processUpdate(item));
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }

    private void chanserv(Long chatId, Nickname nickName, String text) {
        log.info("chanserv: ({}, {}, {})", chatId, nickName, text);
        String replyNickName = nickName.toString();

        if (members.add(nickName)) {
            sayOnChannel(chatId, "теперь я знаю " + nickName + " \uD83D\uDE0E");
            saveState();
        }

        boolean my = false;
        boolean adminSays = nickName.toString().equals(getConfig().getAdmin());

        for (String s: getConfig().getNames()) {
            if (text.startsWith(s)) {
                my = true;
                log.info("*personal message*");
                text = text.substring(s.length());
                while (text.length() > 0 && (text.charAt(0) == Cave.NICK_SEPARATORS[0] || text.charAt(0) == Cave.NICK_SEPARATORS[1])) {
                    text = text.substring(1).trim();
                }
                break;
            }
        }

        if (adminSays) {
            log.info("*admin message*");
        }

        IncomingMessage msg = new IncomingMessage(chatId, nickName, text, my, adminSays);

        // Admin commands
        if (processCommand(msg)) {
            return;
        }

        // Something else
        Optional<Info2Record> rep = info2.firstMatch(text);
        if (rep.isPresent()) {
            answerInfo2Match(msg, rep.get());
        } else {
            if (my) {
                log.info("Personal message in chat: {}", text);
                rep = info2.firstMatch(text.trim());
                if (rep.isPresent()) {
                    answerInfo2Match(msg, rep.get());
                } else {
                    answerDonno(msg);
                }
            }
        }
    }

    private void saveState() {
        mapper.write(MEMBERS_CACHE, members);
    }

    private void loadState() {
        try {
            Object[] m  =  mapper.read(MEMBERS_CACHE, Nickname[].class);
            if (m != null && m.length > 0) {
                log.info("Loaded MEMBERS cache: {}, {} nickname(s)", MEMBERS_CACHE, m.length);
                for (Object n: m) {
                    members.add((Nickname)n);
                }
                //members = m;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean processCommand(IncomingMessage msg) {
        log.info("processCommand: {}", msg);
        final boolean[] sayOk = { false };
        boolean done = false;

        try {
            String[] result = msg.getText().trim().split(" ", 2);
            if (result == null || result.length < 2) {
                return false; // not a command 100%
            }

            String cmd = result[0];
            String params = result[1];
            msg.setText(params);

            if (msg.isAdminMessage() && msg.isPersonal()) {
                if ("forget".equalsIgnoreCase(cmd) || "нахер".equalsIgnoreCase(cmd)) {
                    for (Nickname e: members) {
                        if (e.toString().equals(params) || e.toString().equals(NewWorld.NICK_PREFIX+params)) {
                            members.remove(e);
                            sayOk[0] = true;
                            break;
                        }
                    }
                }
            }

            if (!sayOk[0] && msg.isPersonal() && "adddate".equalsIgnoreCase(cmd)) {
                try {
                    Plugin soWhat = plugins.get(SoWhat.PLUGIN_NAME);
                    if (soWhat != null) {
                        boolean res = soWhat.processCommand(this, cmd, params, msg, SoWhat.PLUGIN_NAME);
                        log.info("processCommand->{}", res);
                        sayOk[0] = res;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            if (sayOk[0]) {
                String reply = msg.getNickName() + NewWorld.NICK_SEPARATOR + "ok";
                sayOnChannel(msg.getChatId(), reply);
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            String reply = msg.getNickName() + NewWorld.NICK_SEPARATOR + ex.getMessage();
            sayOnChannel(msg.getChatId(), reply);
            return true;
        }

        return false;
    }


    private void answerInfo2Match(IncomingMessage msg, Info2Record inforec) {
        log.info("answerInfo2Match: {} {}", msg, inforec);
        boolean answerDonno = true;

        //answerDonno = false;

        if (StringUtils.isBlank(inforec.getValue())) {
            log.info("Blank INFO2 for: {}", msg.getText());
            return;
        }

        if (inforec.getValue().length() > 1 && (inforec.getValue().charAt(0) == Cave.RDB_PREFIX || inforec.getValue().charAt(0) == Cave.RDB_PREFIX_2)) {
            String rdbName = inforec.getValue().substring(1);
            RDBResource r = getRdbByName(rdbName);

            if (r != null) {
                String reply = r.nextSring();
                answerText(msg, reply);
                //answerDonno = false;
            }

            // Don't show wrong RDBs
            answerDonno = false;
        }

        // Not an RDB
        if (answerDonno && inforec.getValue().length() > 1 && inforec.getValue().charAt(0) == Cave.SCRIPT_PREFIX) {
            // Script output
            answerScript(msg, inforec.getValue().substring(1));
            answerDonno = false;
        }

        // Plugin?
        if (answerDonno && inforec.getValue().length() > 1 && inforec.getValue().charAt(0) == Cave.PLUGIN_PREFIX) {
            answerPlugin(msg, inforec.getValue().substring(1));
            answerDonno = false;
        }

        // Not a script or RDB
        if (answerDonno) {
            answerText(msg, inforec.getValue());
            answerDonno = false;
        }
    }

    private void answerScript(IncomingMessage msg, String scriptName) {
        log.info("answerScript: {} {}", msg, scriptName);

        String binary = getConfig().getScriptDir(scriptName + NewWorld.SCRIPT_SUFFIX);
        try {
            if (!Files.exists(Paths.get(binary))) {
                log.error("Script not found: {}", binary);
                return;
            }

            int numAttempts = 3; // : )

            while (numAttempts > 0) {
                try {
                    ExecCommand ec = new ExecCommand(binary);
                    Thread.sleep(100);
                    //ec.getOutput();
                    String res = ec.output;
                    if (StringUtils.isNotBlank(res)) {
                        res = msg.getNickName() + NewWorld.NICK_SEPARATOR + res;
                        sayOnChannel(msg.getChatId(), res);
                        numAttempts = 0;
                    } else {
                        log.error("answerScript zero reply, retry: " + numAttempts);
                        numAttempts--;
                        Thread.sleep(500);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void answerPlugin(IncomingMessage msg, String pluginName) {
        log.info("answerPlugin: {} {}", msg, pluginName);

        try {
                Plugin p = plugins.get(pluginName);
                if (p != null) {
                    boolean res = p.process(this, msg, pluginName);
                    log.info("answerPlugin->{}", res);
                }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void answerText(IncomingMessage msg, String text) {
        log.info("answerText: {} {}", msg, text);
        String fullText = caveReplace(msg.getChatId(), text, msg.getNickName());
        sayOnChannel(msg.getChatId(), fullText);
    }

    private String caveReplace(Long chatId, String text, Nickname nickName) {
        String res = text.replaceAll("N~", nickName.toString()).replaceAll("R~", randomNick().toString()).replaceAll(Cave.LINE_SEPARATOR, NewWorld.LINE_SEPARATOR);
        if (res.startsWith("+")) {
            res = "/me " + res.substring(1);
        }
        return res;
    }

    public void answerDonno(IncomingMessage msg) {
        RDBResource dn = getRdbByName(DONNO_RDB);

        log.info("DONNO: {}", msg.getNickName());

        if (dn == null)
            return;

        String fullText = caveReplace(msg.getChatId(), dn.nextSring(), msg.getNickName());
        sayOnChannel(msg.getChatId(), fullText);
    }

    public void answerRdb(IncomingMessage msg, String rdb) {
        try {
            RDBResource dn = getRdbByName(rdb);
            log.info("RDB: {}", rdb);

            if (dn == null)
                return;

            String fullText = caveReplace(msg.getChatId(), dn.nextSring(), msg.getNickName());
            sayOnChannel(msg.getChatId(), fullText);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public RDBResource getRdbByName(String name) {
        RDBResource res = rdbStore.get(name);
        if (res != null)
            return res;

        RDBResource newRes = new RDBResource(getConfig().getDataDir(name + Cave.RDB_FILE_EXTENSION));

        if (newRes.isAvailabe()) {
            log.info("Adding RDB to cache: {}", name);
            rdbStore.put(name, newRes);
            return newRes;
        }
        return null;
    }

    public Nickname randomNick() {
        int rPos = RDBResource.RNG.nextInt(members.size());
        return (Nickname)(members.toArray()[rPos]);
    }

    private static String toJson(Update u) {
        return "{ \"update_id\":\"" + u.updateId() + "\", \"message\":\"" + u.message() + "\", \"edited_message\":\"" + u.editedMessage() + "\", \"channel_post\":\"" + u.channelPost() + "\", \"edited_channel_post\":\"" + u.editedChannelPost() + "\", \"inline_query\":\"" + u.inlineQuery() + "\", \"chosen_inline_result\":\"" + u.chosenInlineResult() + "\", \"callback_query\":\"" + u.callbackQuery() + "\", \"shipping_query\":\"" + u.shippingQuery() + "\", \"pre_checkout_query\":\"" + u.preCheckoutQuery() + "\" }";
    }

    private static void my_tests(StandaloneBot bot) {
        log.info("*** DEBUG MODE ***");

        //bot.chanserv(-1001082390874L, new Nickname("zloy", true), "wz");
        //bot.chanserv(-1001082390874L, new Nickname("zloy", true), "123");

        // Dec 26/2018
        //TransactionalStorage ts = TransactionalStorage.getInstance();
        //irc.tula.tg.core.db.entity.User u = ts.getUser(0);
        //irc.tula.tg.core.db.entity.User u = ts.findUser("говно");

        //bot.chanserv(-1001082390874L, new Nickname("ncuxonycbka", true), "@rottenbot2018_bot нахер @ncuxonycbka");

        // adddate
        //bot.chanserv(-1001082390874L, new Nickname("ncuxonycbka", true), "@rottenbot2018_bot adddate 09/01/2018 added adddate");
        bot.chanserv(-1001082390874L, new Nickname("zloy", true), "123");


        // fake members
        /*
        bot.members.add(new Nickname("User1", true));
        bot.members.add(new Nickname("User2", true));
        bot.members.add(new Nickname("User3", false));
        */

        //bot.chanserv(-1001082390874L, new Nickname("zloy", true), "fа кто в жопе");
        //bot.chanserv(-1001082390874L, new Nickname("zloy", true), "а кто анус");
        //bot.chanserv(-1001082390874L, new Nickname("zloy", true), "скажи частушку");
        //bot.chanserv(-1001082390874L, new Nickname("zloy", true), "гнилой, скажи частушку");
        //bot.chanserv(-1001082390874L, new Nickname("zloy", true), "гнилой, 1234");
        //bot.chanserv(-1001082390874L, new Nickname("zloy", true), "1");
    }
}
