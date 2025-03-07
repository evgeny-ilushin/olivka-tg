package irc.tula.tg;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.request.ChatAction;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.*;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.SendResponse;
import irc.tula.tg.entity.IncomingMessage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Optional;

@Slf4j
public class BotCore {
    private static final int LONG_SENTENSE = 20;
    private static final String STICKER_PREFIX = "sticker:";

    @Getter
    protected final TelegramBot tg;

    @Getter
    protected final BotConfig config;

    public BotCore(BotConfig config) {
        this.config = checkConfig(config);
        tg = new TelegramBot(config.getToken());
    }

    private BotConfig checkConfig(BotConfig config) {
        if (config != null) {
            if (StringUtils.isBlank(config.getEncoding())) {
                config.setEncoding(Cave.getEncoding());
            }
            log.info("Bot config: {}", config.asTable());
        }
        return config;
    }

    protected void start(UpdatesListener cb) {
        // Cancell WH and start long polling
        //val cwh = cancelWebhook();

        tg.setUpdatesListener(cb);

        // Loop forever : )
        while (true) { sleep(1000); }
    }

    public void sleep(int msec) {
        try { Thread.sleep(msec); } catch (Exception e) { log.error("Thread.sleep: {}", e); }
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

    public void typeOnChannel(Long chatId) {
        if (config.isDebug()) {
            log.info("FAKE TYPEINFO: {}", chatId);
        } else
            {
            try {
                tg.execute(new SendChatAction(chatId, ChatAction.typing), null);
                sleep(1000);
            } catch (Exception ex) {
                log.error("typeOnChannel: {}", ex);
            }
        }
    }

    public void sendImageToChat(Long chatId, String pathToFile) {
        if (config.isDebug()) {
            log.info("IMAGE -> {}: {}", chatId, pathToFile);
        } else
        {
            try {
                BaseRequest a = new SendPhoto(chatId, new File(pathToFile));
                val sendResponse = tg.execute(a);
                boolean ok = sendResponse.isOk();

                log.info("TG.send: {}", sendResponse);
            } catch (Exception ex) {
                log.error("sendImageToChat: {}", ex);
            }
        }
    }

    public Optional<Message> sayOnChannel(IncomingMessage msg, String text) {
        String reply = msg.isPersonal()? text : msg.getNickName() + NewWorld.NICK_SEPARATOR + text;
        Integer originalMessageId = msg.getOriginalMessage() != null? msg.getOriginalMessage().messageId() : null;
        log.info("TG.originalMessageId: {}", originalMessageId);
        return sayOnChannel(msg.getChatId(), reply, msg.isPersonal()? originalMessageId : null);
    }

    public Optional<Message> sayOnChannel(Long chatId, String text, Integer replyToMessageId) {
        log.info("TG.sending to {}-{}: {}", chatId, replyToMessageId, text);

        // Typing notification
        if (!config.isDebug() && config.getAlwaysShowTyping() || (text != null && text.length() > LONG_SENTENSE)) {
            typeOnChannel(chatId);
        }

        try {
            // Detect sticker
            if (text.startsWith(STICKER_PREFIX)) {
                String stickerId = text.substring(STICKER_PREFIX.length());
                Optional<Message> res = sendSticker(chatId, stickerId);
                if (res.isPresent() && res.get().messageId() != null) {
                    return Optional.of(res.get());
                }
            }

            if (config.isDebug()) {
                if (config.getDebugChatId() != null) {
                    chatId = config.getDebugChatId();
                    log.info("DEBUG SEND to chatId {}: {}", chatId, text);
                } else {
                    log.info("FAKE SEND: {} {}", chatId, text);
                    System.err.println("FAKE SEND: {" + chatId + "}: " + text);
                    return Optional.empty();
                }
            }

            SendMessage request = new SendMessage(chatId, text)
                    .parseMode(ParseMode.HTML)
                    .disableWebPagePreview(true)
                    .disableNotification(true)
                    //.replyMarkup(new ForceReply())
                    ;

            if (replyToMessageId != null) {
                request.replyToMessageId(replyToMessageId);
            }

            /*
            if (options !=null) {
                List<KeyboardButton> buttons = new ArrayList<>();
                options.forEach(opt -> buttons.add(new KeyboardButton(opt)));
                Keyboard keyboard = new ReplyKeyboardMarkup(buttons.toArray(new KeyboardButton[0]));
                request.replyMarkup(keyboard);
            }
            */

            SendResponse sendResponse = tg.execute(request);
            boolean ok = sendResponse.isOk();
            log.info("TG.send: {}, {} - {}", sendResponse, sendResponse.message(), ok ? "OK" : ("FAILED: " + sendResponse.description()));
            return Optional.of(sendResponse.message());

        } catch (Exception ex) {
            log.error("sayOnChannel: {}", ex);
            return Optional.empty();
        }
    }

    public Optional<Message> sendSticker(Long chatId, String text) {
        if (config.isDebug()) {
            log.info("FAKE SEND_STICKER: {} {}", chatId, text);
            return Optional.empty();
        } else {

            /*
            // Typing notification
            if (text != null && text.length() > LONG_SENTENSE) {
                typeOnChannel(chatId);
            }
            */

            try {
                SendSticker request = new SendSticker(chatId, text);
                SendResponse sendResponse = tg.execute(request);
                boolean ok = sendResponse.isOk();
                log.info("TG.sendSticker: {}, {} - {}", sendResponse, sendResponse.message(), ok? "OK" : "FAILED");
                return Optional.of(sendResponse.message());
            } catch (Exception ex) {
                log.error("sendSticker: {}", ex);
                return Optional.empty();
            }
        }
    }
}
