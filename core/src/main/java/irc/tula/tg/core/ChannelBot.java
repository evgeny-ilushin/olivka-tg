package irc.tula.tg.core;

import com.pengrad.telegrambot.model.Message;
import irc.tula.tg.core.data.MyObjectMapper;
import irc.tula.tg.util.TextLog;

import java.util.HashSet;
import java.util.Optional;

public interface ChannelBot {
    Optional<Message> sayOnChannel(Long chatId, String text);
    void answerDonno(Long chatId, Nickname nickName);
    void answerRdb(Long chatId, Nickname nickName, String rdb);
    Nickname randomNick();
    RDBResource getRdbByName(String name);
    TextLog getCallbacks();
    HashSet<Nickname> getMembers();
    Info2Resource getInfo2();
    MyObjectMapper getMapper();
    BotConfig getConfig();
}
