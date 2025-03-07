package irc.tula.tg;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import irc.tula.tg.data.JsonObjectMapper;
import irc.tula.tg.data.MyObjectMapper;
import irc.tula.tg.entity.IncomingMessage;
import irc.tula.tg.entity.Nickname;
import irc.tula.tg.plugin.*;
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
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

@Slf4j
public class StandaloneBot extends BotCore implements UpdatesListener, ChannelBot {
    private static final String DONNO_RDB = "donno";

    private static final String MEMBERS_CACHE = "members.json";
    private static final int MIN_AUTOMATH_LENGTH = 3;

    private static final boolean QIFY_UNKNOWN = true;

    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy, HH:MM z");

    // Answered messages
    Cache<String, String> msgCache = null;

    public BotConfig getConfig() { return config; }

    @Getter
    final private TextLog callbacks;

    // Members
    @Getter
    //private HashMap<Integer, Nickname> members = new HashMap<>();
    ChatMembersCache members = new ChatMembersCache();

    // Info2
    @Getter
    final Info2Resource info2 = new Info2Resource(getConfig().getDataDirName(), config.getEncoding());

    // RDBs
    @Getter
    private final HashMap<String, RDBResource> rdbStore = new HashMap<>();

    // DBs
    @Getter
    private final MyObjectMapper mapper;

    // Pluigins
    final HashMap<String, Plugin> plugins = new HashMap<>();

    public StandaloneBot(BotConfig config) {
        super(config);
        callbacks = new TextLog(getConfig().getLogDir("updates.log"));
        mapper = new JsonObjectMapper(getConfig().getDataDirName());
        loadCaches();
        loadPlugins();
    }

    public static void main(String[] args) {
        try {
            if (args.length != 1) {
                log.error("Usage: BOT_JAR <path to config.json>");
                // Sample
                log.error("Sample config:\n\t" + JsonObjectMapper.dumpConfig(BotConfig.getSample()));
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
            } else {
                log.error("Invalid configuration: {}", cfgPath);
            }
        } catch (Exception ex) {
            log.error("Bot crashed (config: {}): {}", args[0], ex);
            ex.printStackTrace();
        }
    }

    private static void consoleSession(StandaloneBot bot) throws Exception {

        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        String text;

        if (bot == null) {
            BotConfig cfg = BotConfig.getSample();
            cfg.setDebug(true);
            bot = new StandaloneBot(cfg);
        }

        while (true) {
            System.out.print("talkToMe> ");
            text = reader.readLine();
            System.out.println("" + bot.chanserv(text));
        }
    }

    private void addPlugin(Plugin plugin) {
        if (plugin.getNames() != null) {
            plugin.getNames().forEach(i -> plugins.put(i, plugin));
        }
        plugins.put(plugin.getName(), plugin);
    }

    private void loadPlugins() {
        Arrays.stream(new Plugin[] {
                new SoWhat(),
                new YWeather(),
                new WebCap(),
                new Sticker(),
                new Goodok(),
                new Glotok(),
                new Wiki()
        }).forEach(this::addPlugin);
        plugins.forEach((k,v) -> v.initialize(this));
    }

    protected boolean wasAnsweredRecently(Update update) {
        Integer id = idOf(update);

        if (msgCache != null && id != null) {

            try {
                String res = msgCache.get(""+id, new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        throw new Exception();
                    }
                });
                if (res != null) {
                    log.info("### MSG Cache: {} was already answered", id);
                    return true;
                }
            } catch (Exception e) {
                //log.error("error: {}", e);
            }
        }
        return false;
    }

    protected void addToAnsweredCache(Update update) {
        Integer id = idOf(update);

        if (msgCache != null && id != null) {
            log.info("### MSG Cache: adding {} to answered", id);
            msgCache.put("" + id, ""+ new Date());
        }
    }

    protected Integer idOf(Update update) {
        if (update.message() != null) {
            return update.message().messageId();
        }
        if (update.editedMessage() != null) {
            return update.editedMessage().messageId();
        }
        if (update.channelPost() != null) {
            return update.channelPost().messageId();
        }
        if (update.editedChannelPost() != null) {
            return update.editedChannelPost().messageId();
        }
        //return new Integer(0);
        return null;
    }

    protected boolean onUpdate(Update update) {
        callbacks.add(toJson(update));
        boolean answered = false;

        Message m = update.message();
        String mt = "message";

        if (m == null) {
            m = update.editedMessage();
            mt = "edited_message";
        }

        // Check if already answered
        if (wasAnsweredRecently(update)) {
            log.info("### Question was already answered.");
            return false;
        }

        // Not supported yet
        /*
        if (m == null) {
            m = update.channelPost();
            mt = "channel_post";
        }
        if (m == null) {
            m = update.editedChannelPost();
            mt = "edited_channel_post";
        }
        */

        log.info("TG Update type: {}", mt);

        if (m != null) {
            User from = m.from();
            String nickName = from.username();
            Nickname nick;

            nick = members.get(m.chat().id(), from.id());

            if (nick == null) {
                // Bullshit
                if (nickName == null) {
                    nickName = from.firstName();
                    nick = new Nickname(from.id(), nickName, false);
                } else {
                    nick = new Nickname(from.id(), nickName, true);
                }
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
                    answered = chanserv(nick, m);
                    if (answered) {
                        addToAnsweredCache(update);
                    }
                }
            }
        }
        return answered;
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

    private boolean chanserv(Nickname nick, Message message) {
        return chanserv(message.chat().id(), nick, message.text());
    }

    @Deprecated
    public boolean chanserv(Long chatId, Nickname nickName, String text) {
        return chanserv(chatId, nickName, text, null);
    }

    public boolean chanserv(Long chatId, Nickname nickName, String text, Message originalMessage) {
        boolean answered = false;
        log.info("chanserv: ({}, {}, {})", chatId, nickName, text);
        String replyNickName = nickName.toString();
        Long from_id = nickName.getId();

        boolean ignoreMessage = getConfig().isIgnored(nickName.toString());
        if (ignoreMessage) {
            log.info("*ignoring message from [" + nickName.toString() + "]*");
            return true;
        }

        // Last seen
        nickName.notice();

        Nickname nCache = members.get(chatId, from_id);

        if (nCache != null) {
            // Already seen as different nickName
            if (!(""+nCache).equals(""+nickName)) {
                // Renamed
                //members.put(chatId, from_id, nickName);
                sayOnChannel(chatId, "теперь я знаю " + nCache + " как " + nickName + " \uD83D\uDE0E");
                //saveState();
            } else {
                // Just update lastSeen
                members.put(chatId, from_id, nickName);
                saveState();
            }
        } else {
            members.put(chatId, from_id, nickName);
            sayOnChannel(chatId, "теперь я знаю " + nickName + " \uD83D\uDE0E");
            saveState();
        }

        boolean my = false;
        boolean adminSays = getConfig().isAdmin(nickName.toString());

        for (String s: getConfig().getNames()) {
            if (text.startsWith(s)) {
                my = true;
                log.info("*personal message*");
                text = text.substring(s.length()).trim();
                while (text.length() > 0 && (text.charAt(0) == Cave.NICK_SEPARATORS[0] || text.charAt(0) == Cave.NICK_SEPARATORS[1])) {
                    text = text.substring(1).trim();
                }
                break;
            }
        }

        if (adminSays) {
            log.info("*admin message*");
        }

        IncomingMessage msg = new IncomingMessage(chatId, nickName, text, my, adminSays, originalMessage);

        // Admin commands
        if (processCommand(msg)) {
            return true;
        }

        // Some auto math
        if (processAutoCalc(msg)) {
            return true;
        }

        // Some bare nick name
        if (processNicknameInfo(msg)) {
            return true;
        }

        // Something else
        Optional<Info2Record> rep = info2.firstMatch(text);
        if (rep.isPresent()) {
            answerInfo2Match(msg, rep.get());
            answered = true;
        } else {
            if (my) {
                log.info("Personal message in chat: {}", text);
                rep = info2.firstMatch(text.trim());
                if (rep.isPresent()) {
                    answerInfo2Match(msg, rep.get());
                    answered = true;
                } else {
                    if (QIFY_UNKNOWN) {
                        answerQified(msg);
                    } else {
                        answerDonno(msg);
                    }
                }
            }
        }
        return answered;
    }

    private void answerQified(IncomingMessage msg) {
        try {
            String qtext = Qify.text(msg.getText());
            if (qtext != null && !qtext.equals(msg.getText())) {
                String reply = constructWhatToSayOnChannel(msg, qtext.trim());
                sayOnChannel(msg, reply);
            } else {
                answerDonno(msg);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveState() {
        try {
            new File(MEMBERS_CACHE).delete();
            mapper.write(MEMBERS_CACHE, members);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void loadCaches() {
        try {
            ChatMembersCache m  =  mapper.read(MEMBERS_CACHE, ChatMembersCache.class);
            if (m != null) {
                members = m;
            }

            msgCache = CacheBuilder.newBuilder()
                    .maximumSize(10000)
                    .expireAfterWrite(10, TimeUnit.MINUTES)
                    .build();

            log.info("### MSG Cache created: {}", msgCache.stats());
        }
        catch (Exception ex) {
            log.info("No MEMBERS file");
            ex.printStackTrace();
        }
    }

    private static boolean looksLikeMathOrNot(String src) {
        // long enough
        if (src == null || src.trim().length() < MIN_AUTOMATH_LENGTH) {
            return false;
        }

        src = src.trim();

        // contains proper chars
        final String valid = "0123456789.-+*^/%() ";
        for (int i = 0; i < src.length(); i++) {
            if (valid.indexOf(src.charAt(i)) < 0) {
                return false;
            }
        }
        // at least 2 digits + 1 operator
        int numd = 0, numo = 0;
        final String validOps = "-+*^/%()";
        final String validDgs = "0123456789";
        for (int i = 0; i < src.length(); i++) {
            if (validOps.indexOf(src.charAt(i)) > -1) {
                numo++;
            }
            else if (validDgs.indexOf(src.charAt(i)) > -1) {
                numd++;
            }
        }

        return numo >= 1 && numd >= 2;
    }

    private boolean processAutoCalc(IncomingMessage msg) {
        try {
            String mt = msg.getText();
            if (mt.toLowerCase().startsWith("calc ")) {
                mt = mt.substring(5).trim();
                answerScriptEx(msg, "calc", mt);
                return true;
            }
            if (looksLikeMathOrNot(mt)) {
                answerScriptEx(msg, "calc", mt);
                return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private boolean processNicknameInfo(IncomingMessage msg) {
        Nickname speaker = members.get(msg.getChatId(), msg.getNickName().getId());
        if (speaker != null) {
            if (msg.getText() != null && (msg.getText().equals(speaker.getText()) || msg.getText().equals(speaker.toString()))) {
                sayOnChannel(msg.getChatId(), msg.getNickName() + NewWorld.NICK_SEPARATOR + "это ты");
                return true;
            }
            for (Nickname n : members.list(msg.getChatId())) {
                if (msg.getText().equals(n.getText()) || msg.getText().equals(n.toString())) {
                    showPublicNickInfo(msg, n);
                    return true;
                }
            }
        }
        return false;
    }

    private void showPublicNickInfo(IncomingMessage msg, Nickname n) {
        ZonedDateTime seen = n.getLastSeen().toInstant().atZone(ZoneId.of("Europe/Moscow"));
        sayOnChannel(msg.getChatId(), msg.getNickName() + NewWorld.NICK_SEPARATOR + n + " последний раз мяукал " + simpleDateFormat.format(n.getLastSeen()));
    }

    private boolean processCommand(IncomingMessage msg) {
        log.info("processCommand: {}", msg);
        final boolean[] sayOk = { false };
        boolean done = false;
        String sayOkText = "ok";

        try {
            String[] result = msg.getText().trim().split(" ", 2);
            if (result == null || result.length < 2) {
                return false; // not a command 100%
            }

            String cmd = result[0];
            String params = result[1];
            //msg.setText(params.trim());
            msg.setWasTrimmedToParams(true);

            // 1
            if (msg.isAdminMessage() /* && msg.isPersonal() */ ) {
                if ("forget".equalsIgnoreCase(cmd) || "нахер".equalsIgnoreCase(cmd)) {
                    if ("all".equalsIgnoreCase(params) || "всех".equalsIgnoreCase(params)) {
                        ChatMembersCache admins = new ChatMembersCache();
                        for (Nickname e : members.list(msg.getChatId())) {
                            if (getConfig().isAdmin(e.toString())) {
                                admins.put(msg.getChatId(), e.getId(), e);
                            }
                        }
                        members = admins;
                        sayOk[0] = true;
                        saveState();
                    }
                    else {
                        for (Nickname e : members.list(msg.getChatId())) {
                            if (e.toString().equals(params) || e.toString().equals(NewWorld.NICK_PREFIX + params)) {
                                members.remove(msg.getChatId(), e.getId());
                                sayOk[0] = true;
                                saveState();
                                break;
                            }
                        }
                    }
                }
            }

            // 2
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

            // 2.1
            if (!sayOk[0] && msg.isPersonal()  && "пробей".equalsIgnoreCase(cmd)) {
                try {
                    log.info("processCommand->{}");
                    sayOk[0] = true;
                    //sayOkText =
                    answerScriptEx(msg, "mlookup", params);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    answerDonno(msg);
                }
                return true;
            }

            // 3
            if (!sayOk[0] && msg.isPersonal() && ("status".equalsIgnoreCase(cmd) || "статус".equalsIgnoreCase(cmd))) {
                try {
                        sayOk[0] = false;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            // 4
            if (msg.isAdminMessage() && !sayOk[0] && msg.isPersonal() && ("клизма".equalsIgnoreCase(cmd) || "purge".equalsIgnoreCase(cmd))) {
                try {
                    sayOk[0] = true;
                    //4444
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            if (sayOk[0]) {
                String reply = msg.getNickName() + NewWorld.NICK_SEPARATOR + sayOkText;
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
                String reply = nextString(r);
                answerText(msg, reply);
                //answerDonno = false;
            }

            // Don't show wrong RDBs
            answerDonno = false;
        }

        // Not an RDB
        if (answerDonno && inforec.getValue().length() > 2 && inforec.getValue().charAt(0) == Cave.SCRIPT_PREFIX && inforec.getValue().charAt(1) == Cave.SCRIPT_PREFIX) {
            // Script with params output
            if (StringUtils.isNotBlank(msg.getText()) && msg.getText().trim().length() > 1 && msg.isWasTrimmedToParams()) {
                answerScriptEx(msg, inforec.getValue().substring(2), msg.getText());
            }
            answerDonno = false;
        }

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
                        res = constructWhatToSayOnChannel(msg, res);
                        sayOnChannel(msg, res);
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

    private void answerScriptEx(IncomingMessage msg, String scriptName, String params) {
        log.info("answerScriptEx: {} {}", msg, scriptName);

        String binary = getConfig().getScriptDir(scriptName + NewWorld.SCRIPT_SUFFIX);
        try {
            if (!Files.exists(Paths.get(binary))) {
                log.error("Script not found: {}", binary);
                return;
            }

            int numAttempts = 3; // : )

            while (numAttempts > 0) {
                try {
                    ExecCommand ec = new ExecCommand(binary + " " + params);
                    Thread.sleep(100);
                    //ec.getOutput();
                    String res = ec.output;
                    if (StringUtils.isNotBlank(res)) {
                        res = constructWhatToSayOnChannel(msg, res);
                        sayOnChannel(msg, res);
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

    private String constructWhatToSayOnChannel(IncomingMessage msg, String res) {
        return msg.isPersonal()? res : msg.getNickName() + NewWorld.NICK_SEPARATOR + res;
    }

    private void answerPlugin(IncomingMessage msg, String text) {
        String pluginName = text;
        String[] params = null;

        log.info("answerPlugin: {} {}", msg, pluginName);

        try {
                String[] parts = text.split(" ");

                if (parts != null && parts.length > 1) {
                    pluginName = parts[0];
                    params = Arrays.copyOfRange(parts, 1, parts.length);
                }

                Plugin p = plugins.get(pluginName);
                if (p != null) {
                    boolean res = p.process(this, msg, pluginName, params);
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
        String res = text.replaceAll("N~", nickName.toString()).replaceAll("R~", randomNick(chatId).toString()).replaceAll(Cave.LINE_SEPARATOR, NewWorld.LINE_SEPARATOR);
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

        String fullText = caveReplace(msg.getChatId(), nextString(dn), msg.getNickName());
        sayOnChannel(msg.getChatId(), fullText);
    }

    public String nextString(RDBResource r) {
        String res = r.nextString();
        return rdbDeep(res);
    }

    public void answerRdb(IncomingMessage msg, String rdb) {
        try {
            RDBResource dn = getRdbByName(rdb);
            log.info("RDB: {}", rdb);

            if (dn == null)
                return;

            String fullText = caveReplace(msg.getChatId(), nextString(dn), msg.getNickName());
            sayOnChannel(msg.getChatId(), fullText);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public RDBResource getRdbByName(String name) {
        RDBResource res = rdbStore.get(name);
        if (res != null)
            return res;

        RDBResource newRes = new RDBResource(getConfig().getDataDir(name + Cave.RDB_FILE_EXTENSION), config.getEncoding());

        if (newRes.isAvailabe()) {
            log.info("Adding RDB to cache: {}", name);
            rdbStore.put(name, newRes);
            return newRes;
        }
        return null;
    }

    public Nickname randomNick(Long chatId) {
        return members.randomAt(chatId);
    }

    private static String toJson(Update u) {
        return "{ \"update_id\":\"" + u.updateId() + "\", \"message\":\"" + u.message() + "\", \"edited_message\":\"" + u.editedMessage() + "\", \"channel_post\":\"" + u.channelPost() + "\", \"edited_channel_post\":\"" + u.editedChannelPost() + "\", \"inline_query\":\"" + u.inlineQuery() + "\", \"chosen_inline_result\":\"" + u.chosenInlineResult() + "\", \"callback_query\":\"" + u.callbackQuery() + "\", \"shipping_query\":\"" + u.shippingQuery() + "\", \"pre_checkout_query\":\"" + u.preCheckoutQuery() + "\" }";
    }

    private String rdbDeep(String source) {
        StringBuilder res = new StringBuilder();
        Character vAct = '$', vLb = '{', vRb = '}';
        if (!source.contains("$")) {
            //log.info("no inner RDBs");
            return source;
        }

        char[] s = source.toCharArray();
        int p = 0;

        do {
            char c = s[p];
            boolean cp = true;

            if (c == vAct && (s.length - p) > 3 /* "${}" */
            && (s[p+1] == vLb)) {
                int p1 = p+2;
                while (Character.isLetterOrDigit(s[p1]) && p1 < s.length) {
                    p1++;
                }
                if (s[p1] == vRb) {
                    String rdb = source.substring(p+2, p1);
                    log.info("Got it: {}", rdb);
                    RDBResource r = getRdbByName(rdb);
                    if (r != null) {
                        String innerText = nextString(r);
                        if (StringUtils.isNotBlank(innerText)) {
                            res.append(innerText);
                            log.info("Got inner RDB: {} -> {}", rdb, innerText);
                            cp = false;
                            p = p1;
                        }

                    }
                    else {
                    }
                }

            }

            if (cp) {
                res.append(c);
            }

        } while (++p < s.length);

        return res.toString();
    }

    private static void my_tests(StandaloneBot bot) {

        //boolean f = looksLikeMathOrNot("2+2");
        //f =looksLikeMathOrNot("0.5+ 3*4");


        log.info("*** DEBUG MODE ***");
        long CHAT = -1001082390874L;

        //SoWhat soWhat = (SoWhat)bot.plugins.get("sowhat");
        //soWhat.dumpSortedCSV();

        //bot.chanserv(-100108239087L, new Nickname(44, "zloy", true), "ну что?");
        //bot.chanserv(-100108239087L, new Nickname(44, "zloy", true), "ну так что?");

        //Update u = new Update();

        //bot.loadCore();
        //boolean r = bot.wasAnsweredRecently(u);
        //bot.addToAnsweredCache(u);
        //r = bot.wasAnsweredRecently(u);

        //boolean csRes = bot.chanserv(-1001082390874L, new Nickname(1, "zloy", true), "444");
        //boolean csRes = bot.chanserv(-1001082390874L, new Nickname(1, "zloy", true), "!ru  cow");
        //boolean csRes = bot.chanserv(-1001082390874L, new Nickname(1, "zloy", true), "!en  свинья");

        // 2020
        //boolean csRes = bot.chanserv(-1001082390874L, new Nickname(1, "zloy", true), "calc 209.4+2");
        // Auto calc
        /*
        boolean bx = looksLikeMathOrNot("123");
        bx =looksLikeMathOrNot("1+2");
        bx =looksLikeMathOrNot("0.5+ 3*4");
        bx =looksLikeMathOrNot("но это уже так... я это решаю через магнитный браслет\n" +
                "<Hermit_W> вот такой https://tula.vseinstrumenti.ru/spetsodezhda/sumki-kejsy/r");
        bx =looksLikeMathOrNot("нет");
        */
        //boolean csRes = bot.chanserv(-1001082390874L, new Nickname(10, "123", true), "гнилой, кохуита ");

        //boolean csRes = bot.chanserv(-1001082390874L, new Nickname(10L, "zloy", true), "глоток");

        boolean csRes = bot.chanserv(-547476212L, new Nickname(412132074L, "@ncuxonycbka", true), "wiki ssd");


        if (true) {
            return;
        }

        csRes = bot.chanserv(-1001082390874L, new Nickname(10L, "zloy", true), "2+2");
        csRes = bot.chanserv(-100108239087L, new Nickname(11L, "zloy1", true), "2+2 43 324");
        csRes = bot.chanserv(-100108239087L, new Nickname(12L, "zloy2", true), "маро");
        csRes = bot.chanserv(-100108239087L, new Nickname(13L, "zloy3", true), "мас");

        //bot.chanserv(-1001082390874L, new Nickname("zloy", true), "123");

        // Dec 26/2018
        //TransactionalStorage ts = TransactionalStorage.getInstance();
        //irc.tula.tg.db.entity.User u = ts.getUser(0);
        //irc.tula.tg.db.entity.User u = ts.findUser("говно");

        //bot.chanserv(-1001082390874L, new Nickname("ncuxonycbka", true), "@rottenbot2018_bot нахер @ncuxonycbka");

        // adddate
        //bot.chanserv(-1001082390874L, new Nickname("ncuxonycbka", true), "@rottenbot2018_bot adddate 09/01/2018 added adddate");
        //bot.typeOnChannel(CHAT);
        //bot.chanserv(CHAT, new Nickname(123, "zloy", true), "123");

        // Inner RDB - 2019
        //String s = bot.rdbDeep(" sigh worgjoijrpgja 140  *Г24 ${dateprefix1}again");
        //log.info("s: {}", s);


        // fake members
        bot.members.put(1L, 1L, new Nickname(1L, "User1", true));
        bot.members.put(1L, 2L, new Nickname(2L, "User2", true));
        bot.members.put(1L, 3L, new Nickname(3L, "User3", false));

        bot.chanserv(-100108239087L, new Nickname(44L, "zloy", true), "а кто в жопе");
        //bot.chanserv(-1001082390874L, new Nickname("zloy", true), "а кто анус");
        //bot.chanserv(-1001082390874L, new Nickname("zloy", true), "скажи частушку");
        //bot.chanserv(-1001082390874L, new Nickname("zloy", true), "гнилой, скажи частушку");
        //bot.chanserv(-1001082390874L, new Nickname("zloy", true), "гнилой, 1234");
        //bot.chanserv(-1001082390874L, new Nickname("zloy", true), "1");

        String pl_dev = "{ \"update_id\":\"18322330\", \"message\":\"null\", \"edited_message\":\"null\", \"channel_post\":\"Message{message_id=6, from=null, date=1565769547, chat=Chat{id=-1001343308314, type=channel, first_name='null', last_name='null', username='olivka_dev', title='olivka development', all_members_are_administrators=null, photo=null, description='null', invite_link='null', pinned_message=null, sticker_set_name='null', can_set_sticker_set=null}, forward_from=null, forward_from_chat=null, forward_from_message_id=null, forward_signature='null', forward_date=null, reply_to_message=null, edit_date=null, media_group_id='null', author_signature='null', text='1', entities=null, caption_entities=null, audio=null, document=null, animation=null, game=null, photo=null, sticker=null, video=null, voice=null, video_note=null, caption='null', contact=null, location=null, venue=null, new_chat_member=null, new_chat_members=null, left_chat_member=null, new_chat_title='null', new_chat_photo=null, delete_chat_photo=null, group_chat_created=null, supergroup_chat_created=null, channel_chat_created=null, migrate_to_chat_id=null, migrate_from_chat_id=null, pinned_message=null, invoice=null, successful_payment=null, connected_website='null', passport_data=null}\", \"edited_channel_post\":\"null\", \"inline_query\":\"null\", \"chosen_inline_result\":\"null\", \"callback_query\":\"null\", \"shipping_query\":\"null\", \"pre_checkout_query\":\"null\" }";
        String pl_prod = "{ \"update_id\":\"18322331\", \"message\":\"Message{message_id=401037, from=User{id=412132074, is_bot=false, first_name='ncuxonycbka', last_name='null', username='ncuxonycbka', language_code='en'}, date=1565769632, chat=Chat{id=-1001082390874, type=supergroup, first_name='null', last_name='null', username='tulairc', title='irc.tula.net', all_members_are_administrators=null, photo=null, description='null', invite_link='null', pinned_message=null, sticker_set_name='null', can_set_sticker_set=null}, forward_from=null, forward_from_chat=null, forward_from_message_id=null, forward_signature='null', forward_date=null, reply_to_message=null, edit_date=null, media_group_id='null', author_signature='null', text='2', entities=null, caption_entities=null, audio=null, document=null, animation=null, game=null, photo=null, sticker=null, video=null, voice=null, video_note=null, caption='null', contact=null, location=null, venue=null, new_chat_member=null, new_chat_members=null, left_chat_member=null, new_chat_title='null', new_chat_photo=null, delete_chat_photo=null, group_chat_created=null, supergroup_chat_created=null, channel_chat_created=null, migrate_to_chat_id=null, migrate_from_chat_id=null, pinned_message=null, invoice=null, successful_payment=null, connected_website='null', passport_data=null}\", \"edited_message\":\"null\", \"channel_post\":\"null\", \"edited_channel_post\":\"null\", \"inline_query\":\"null\", \"chosen_inline_result\":\"null\", \"callback_query\":\"null\", \"shipping_query\":\"null\", \"pre_checkout_query\":\"null\" }";
        String p1_dev1 = "{ \"update_id\":\"18322332\", \"message\":\"null\", \"edited_message\":\"null\", \"channel_post\":\"Message{message_id=7, from=null, date=1565769693, chat=Chat{id=-1001343308314, type=channel, first_name='null', last_name='null', username='olivka_dev', title='olivka development', all_members_are_administrators=null, photo=null, description='null', invite_link='null', pinned_message=null, sticker_set_name='null', can_set_sticker_set=null}, forward_from=null, forward_from_chat=null, forward_from_message_id=null, forward_signature='null', forward_date=null, reply_to_message=null, edit_date=null, media_group_id='null', author_signature='null', text='2', entities=null, caption_entities=null, audio=null, document=null, animation=null, game=null, photo=null, sticker=null, video=null, voice=null, video_note=null, caption='null', contact=null, location=null, venue=null, new_chat_member=null, new_chat_members=null, left_chat_member=null, new_chat_title='null', new_chat_photo=null, delete_chat_photo=null, group_chat_created=null, supergroup_chat_created=null, channel_chat_created=null, migrate_to_chat_id=null, migrate_from_chat_id=null, pinned_message=null, invoice=null, successful_payment=null, connected_website='null', passport_data=null}\", \"edited_channel_post\":\"null\", \"inline_query\":\"null\", \"chosen_inline_result\":\"null\", \"callback_query\":\"null\", \"shipping_query\":\"null\", \"pre_checkout_query\":\"null\" }";
    }

    public Object chanserv(Object payload) {
        if (config.isDebug()) {
            return chanserv(-1L, new Nickname(10L, "DEBUG", true), "" + payload);
        }
        return null;
    }
}
