package irc.tula.tg;

import com.pengrad.telegrambot.model.Message;
import irc.tula.tg.data.MyObjectMapper;
import irc.tula.tg.entity.IncomingMessage;
import irc.tula.tg.entity.Nickname;
import irc.tula.tg.util.TextLog;

import java.util.List;
import java.util.Optional;

public interface ChannelBot {
    void typeOnChannel(Long chatId);
    void sendImageToChat(Long chatId, String pathToFile);
    Optional<Message> sayOnChannel(Long chatId, String text, Integer replyToMessageId);
    Optional<Message> sayOnChannel(IncomingMessage msg, String text);
    Optional<Message> sayOnChannelNoPrefix(IncomingMessage msg, String text);
    Optional<Message> sendSticker(Long chatId, String text);
    void answerText(IncomingMessage msg, String reply);
    void answerDonno(IncomingMessage msg);
    void answerRdb(IncomingMessage msg, String rdb);
    Nickname randomNick(Long chatId);
    RDBResource getRdbByName(String name);
    TextLog getCallbacks();
    //HashMap<Integer, Nickname> getMembers();
    Info2Resource getInfo2();
    MyObjectMapper getMapper();
    BotConfig getConfig();
}
