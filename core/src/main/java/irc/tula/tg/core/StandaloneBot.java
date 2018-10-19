package irc.tula.tg.core;

import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import irc.tula.tg.util.TextLog;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

@Slf4j
public class StandaloneBot extends BotCore implements UpdatesListener {

    // olivka
    //private static final String DEFAULT_TOKEN = "294103149:AAGPawepBdjAtu9z9aKDj2rLwwdNt0UDi9E";
    //private static final String[] BOT_NAMES = { "olivka", "оливка", "@OlivkaIrcBot" };

    // rotten
    private static final String DEFAULT_TOKEN = "668163913:AAE98c1hN0O5m1kyE3e9XgBLLQolN96fpH4";
    private static final String[] BOT_NAMES = { "rotten", "гнилой", "@rottenbot2018_bot" };

    private static final String BOT_HOME = "/home/ec2-user/bin/bots/data";
    private static final String INFO2 = "info2.db";
    private static final String DONNO_RDB = "donno";

    private TextLog callbacks = new TextLog(getConfig().getLogDir() + Cave.PATH_SEPARATOR + "updates.log");

    // Members
    private HashSet<Nickname> members = new HashSet<>();

    // Info2
    Info2Resource info2 = new Info2Resource(BOT_HOME, INFO2);

    // RDBs
    private HashMap<String, RDBResource> rdbStore = new HashMap<>();

    public StandaloneBot(String token) {
        super(token);
    }

    public static void main(String[] args) {
        StandaloneBot bot = new StandaloneBot(DEFAULT_TOKEN);

        log.info("Starting bot...");
        bot.start(bot);

        //bot.chanserv(-1001082390874L, new Nickname("zloy", true), "а кто в жопе");

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
        //bot.chanserv(-1001082390874L, new Nickname("zloy", true), "1234");
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
        }

        /*
        if (text.equalsIgnoreCase("1")) {
            sayOnChannel(chatId, replyNickName + ", " + (1 + RDBResource.RNG.nextInt(9)));
        } else
        if (text.equalsIgnoreCase("кто")) {
            Nickname randNick = randomNick();
            sayOnChannel(chatId, replyNickName + ", " + randNick);
        }*/

        Optional<Info2Record> rep = info2.firstMatch(text);
        if (rep.isPresent()) {
            answerInfo2Match(chatId, nickName, text, rep.get());
        } else {
            boolean my = false;
            for (String s: BOT_NAMES) {
                if (text.startsWith(s)) {
                    my = true;
                    text = text.substring(s.length());
                    while (text.length() > 0 && (text.charAt(0) == Cave.NICK_SEPARATORS[0] || text.charAt(0) == Cave.NICK_SEPARATORS[1])) {
                        text = text.substring(1);
                    }
                    break;
                }
            }

            if (my) {
                log.info("Personal message in chat: {}", text);
                rep = info2.firstMatch(text.trim());
                if (rep.isPresent()) {
                    answerInfo2Match(chatId, nickName, text, rep.get());
                } else {
                    answerDonno(chatId, nickName);
                }
            }
        }
    }

    private void answerInfo2Match(Long chatId, Nickname nickName, String text, Info2Record inforec) {
        boolean answerDonno = true;

        //answerDonno = false;

        if (StringUtils.isBlank(inforec.getValue())) {
            log.info("Blank INFO2 for: {}", text);
            return;
        }

        if (inforec.getValue().length() > 1 && (inforec.getValue().charAt(0) == Cave.RDB_PREFIX || inforec.getValue().charAt(0) == Cave.RDB_PREFIX_2)) {
            String rdbName = inforec.getValue().substring(1);
            RDBResource r = getRdbByName(rdbName);

            if (r != null) {
                String reply = r.nextSring();
                answerText(chatId, nickName, reply);
                //answerDonno = false;
            }

            // Don't show wrong RDBs
            answerDonno = false;
        }

        // Not an RDB
        if (answerDonno) {
            answerText(chatId, nickName, inforec.getValue());
            answerDonno = false;
        }
    }

    private void answerText(Long chatId, Nickname nickName, String text) {
        String fullText = caveReplace(chatId, text, nickName);
        sayOnChannel(chatId, fullText);
    }

    private String caveReplace(Long chatId, String text, Nickname nickName) {
        String res = text.replaceAll("N~", nickName.toString()).replaceAll("R~", randomNick().toString());
        if (res.startsWith("+")) {
            res = "/me " + res.substring(1);
        }
        return res;
    }

    private void answerDonno(Long chatId, Nickname nickName) {
        RDBResource dn = getRdbByName(DONNO_RDB);

        log.info("DONNO: {}", nickName);

        if (dn == null)
            return;

        String fullText = caveReplace(chatId, dn.nextSring(), nickName);
        sayOnChannel(chatId, fullText);
    }

    private RDBResource getRdbByName(String name) {
        RDBResource res = rdbStore.get(name);
        if (res != null)
            return res;

        RDBResource newRes = new RDBResource(BOT_HOME + NewWorld.PATH_SEPARATOR + name + Cave.RDB_FILE_EXTENSION);

        if (newRes.isAvailabe()) {
            log.info("Adding RDB to cache: {}", name);
            rdbStore.put(name, newRes);
            return newRes;
        }
        return null;
    }

    private Nickname randomNick() {
        return (Nickname)members.toArray()[RDBResource.RNG.nextInt(members.size())];
    }

    private static String toJson(Update u) {
        return "{ \"update_id\":\"" + u.updateId() + "\", \"message\":\"" + u.message() + "\", \"edited_message\":\"" + u.editedMessage() + "\", \"channel_post\":\"" + u.channelPost() + "\", \"edited_channel_post\":\"" + u.editedChannelPost() + "\", \"inline_query\":\"" + u.inlineQuery() + "\", \"chosen_inline_result\":\"" + u.chosenInlineResult() + "\", \"callback_query\":\"" + u.callbackQuery() + "\", \"shipping_query\":\"" + u.shippingQuery() + "\", \"pre_checkout_query\":\"" + u.preCheckoutQuery() + "\" }";
    }
}

class DefaultBotConfig implements BotConfig {
    @Override
    public String getLogDir() {
        return "/tmp";
    }
}