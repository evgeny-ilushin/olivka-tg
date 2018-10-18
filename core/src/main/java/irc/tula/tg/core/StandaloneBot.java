package irc.tula.tg.core;

import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import irc.tula.tg.util.TextLog;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class StandaloneBot extends BotCore implements UpdatesListener {
    private static final String DEFAULT_TOKEN = "294103149:AAGPawepBdjAtu9z9aKDj2rLwwdNt0UDi9E";

    private TextLog callbacks = new TextLog(getConfig().getLogDir() + Cave.PATH_SEPARATOR + "updates.log");

    public StandaloneBot(String token) {
        super(token);
    }

    public static void main(String[] args) {
        StandaloneBot bot = new StandaloneBot(DEFAULT_TOKEN);

        log.info("Starting bot...");
        bot.start(bot);
    }

    protected void onUpdate(Update update) {
        callbacks.add(toJson(update));

        if (update.message() != null) {
            Message m = update.message();
            User from = m.from();
            String nickName = from.username();

            // Ignore other bots?
            if (from.isBot()) {
            }

            // Chat only
            if (m.chat() != null && m.chat().title() != null) {
                Chat c = m.chat();
                Long replyChatId = c.id();

                // I am text only
                if (m.text() != null) {
                    chanserv(replyChatId, nickName, m.text());
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

    private void chanserv(Long chatId, String nickName, String text) {
        log.info("chanserv: ({}, {}, {})", chatId, nickName, text);
        String replyNickName = NewWorld.NICK_PREFIX + nickName;

        sayOnChannel(chatId, replyNickName + ", иди в задницу");
    }

    private static String toJson(Update u) {
        return "{ \"update_id\":\"" + u.updateId() + "\", \"message\":\"" + u.message() + "\", \"edited_message\":\"" + u.editedMessage() + "\", \"channel_post\":\"" + u.channelPost() + "\", \"edited_channel_post\":\"" + u.editedChannelPost() + "\", \"inline_query\":\"" + u.inlineQuery() + "\", \"chosen_inline_result\":\"" + u.chosenInlineResult() + "\", \"callback_query\":\"" + u.callbackQuery() + "\", \"shipping_query\":\"" + u.shippingQuery() + "\", \"pre_checkout_query\":\"" + u.preCheckoutQuery() + "\" }";
    }
}
