package irc.tula.tg.core;

import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import irc.tula.tg.util.TextLog;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StandaloneBot extends BotCore {
    private static final String DEFAULT_TOKEN = "294103149:AAGPawepBdjAtu9z9aKDj2rLwwdNt0UDi9E";

    private TextLog callbacks = new TextLog(getConfig().getLogDir() + Cave.PATH_SEPARATOR + "updates.log");

    public StandaloneBot(String token) {
        super(token);
    }

    public static void main(String[] args) {
        BotCore bot = new BotCore(DEFAULT_TOKEN);

        log.info("Starting bot...");
        bot.start();
    }

    @Override
    protected void onUpdate(Update update) {
        callbacks.add(toJson(update));

        if (update.message() != null) {
            Message m = update.message();
            User from = m.from();
            String replyNickName = NewWorld.NICK_PREFIX + from.username();

            // Ignore other bots?
            if (from.isBot()) {
            }

            // Chat only
            if (m.chat() != null && m.chat().title() != null) {
                Chat c = m.chat();
                Long replyChatId = c.id();

                // I am text only
                if (m.text() != null) {
                    chanserv(replyChatId, replyNickName, m.text());
                }
            }
        }
    }

    private void chanserv(Long chatId, String replyNickName, String text) {
        sayOnChannel(chatId, replyNickName + ", иди в задницу");
    }

    private static String toJson(Update u) {
        return "{ \"update_id\":\"" + u.updateId() + "\", \"message\":\"" + u.message() + "\", \"edited_message\":\"" + u.editedMessage() + "\", \"channel_post\":\"" + u.channelPost() + "\", \"edited_channel_post\":\"" + u.editedChannelPost() + "\", \"inline_query\":\"" + u.inlineQuery() + "\", \"chosen_inline_result\":\"" + u.chosenInlineResult() + "\", \"callback_query\":\"" + u.callbackQuery() + "\", \"shipping_query\":\"" + u.shippingQuery() + "\", \"pre_checkout_query\":\"" + u.preCheckoutQuery() + "\" }";
    }
}
