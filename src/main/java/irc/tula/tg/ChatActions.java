package irc.tula.tg;

import com.pengrad.telegrambot.model.Message;

import java.util.Optional;

public interface ChatActions {
    void typeOnChannel(Long chatId);
    void sendImageToChat(Long chatId, String pathToFile);
    Optional<Message> sayOnChannel(Long chatId, String text);
    Optional<Message> sendSticker(Long chatId, String text);
}
