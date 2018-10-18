package irc.tula.tg.core;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.request.ForceReply;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetWebhook;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.List;
import java.util.Optional;

@Slf4j
public class BotCore {
    private final TelegramBot tg;

    @Getter
    private final String token;

    @Getter
    private final BotConfig config;

    public BotCore(String token, BotConfig config) {
        this.token = token;
        this.config = new DefaultBotConfig();
        tg = new TelegramBot(token);
    }

    public BotCore(String token) {
        this(token, null);
    }

    protected void start(UpdatesListener cb) {
        // Cancell WH and start long polling
        val cwh = cancelWebhook();

        tg.setUpdatesListener(cb);

        // Loop forever : )
        while (true) { try { Thread.sleep(1000); } catch (Exception e) { log.error("System: {}", e); } }
    }

    public Optional<BaseResponse> cancelWebhook() {
        try {
            SetWebhook request = new SetWebhook()
                    .url(null);
            BaseResponse response = tg.execute(request);
            boolean ok = response.isOk();
            return Optional.of(response);
        } catch (Exception ex) {
            log.error("cancelWebhook: {}", ex);
            return Optional.empty();
        }
    }

    protected Optional<Message> sayOnChannel(Long chatId, String text) {
        try {
            SendMessage request = new SendMessage(chatId, text)
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(true)
                    //.replyToMessageId(1)
                    //.replyMarkup(new ForceReply())
                    ;

            SendResponse sendResponse = tg.execute(request);
            boolean ok = sendResponse.isOk();
            return Optional.of(sendResponse.message());
        } catch (Exception ex) {
            log.error("sayOnChannel: {}", ex);
            return Optional.empty();
        }
    }
}
