package irc.tula.tg.core;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SetWebhook;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class BotCore {
    @Getter
    protected final TelegramBot tg;

    @Getter
    protected final BotConfig config;

    public BotCore(BotConfig config) {
        this.config = config;
        tg = new TelegramBot(config.getToken());
    }

    protected void start(UpdatesListener cb) {
        // Cancell WH and start long polling
        //val cwh = cancelWebhook();

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

    public Optional<Message> sayOnChannel(Long chatId, String text) {
        if (config.isDebug()) {
            log.info("FAKE SEND: {} {}", chatId, text);
            return Optional.empty();
        } else {
            // Typing notification
            if (false) {

            }

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
                log.info("TG.send: {}, {}", sendResponse, sendResponse.message());
                return Optional.of(sendResponse.message());
            } catch (Exception ex) {
                log.error("sayOnChannel: {}", ex);
                return Optional.empty();
            }
        }
    }
}
