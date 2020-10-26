package irc.tula.tg.core;

import com.pengrad.telegrambot.model.Message;
import irc.tula.tg.core.data.MyObjectMapper;
import irc.tula.tg.core.entity.IncomingMessage;
import irc.tula.tg.core.entity.Nickname;
import irc.tula.tg.util.TextLog;

import java.util.HashMap;
import java.util.Optional;

public interface ChannelBot {
    Optional<Message> sayOnChannel(Long chatId, String text);
    void typeOnChannel(Long chatId);
    void sendImageToChat(Long chatId, String pathToFile);
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
