package irc.tula.tg.core;

import irc.tula.tg.core.entity.Nickname;
import lombok.Data;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

@Data
public class ChatMembersCache implements Serializable {

    private HashMap<Long, HashMap<Integer, Nickname>> members;

    public ChatMembersCache() {
        members = new HashMap<>();
    }

    public Nickname get(Long chatId, Integer userId) {
        HashMap<Integer, Nickname> chatMembers = members.get(chatId);
        if (chatMembers != null) {
            return chatMembers.get(userId);
        }
        return null;
    }

    public Nickname put(Long chatId, Integer userId, Nickname nickname) {
        HashMap<Integer, Nickname> chatMembers = members.get(chatId);
        if (chatMembers == null) {
            chatMembers = new HashMap<>();
            chatMembers.put(userId, nickname);
            members.put(chatId, chatMembers);
        }
        else {
            chatMembers.put(userId, nickname);
        }
        return nickname;
    }

    public Collection<Nickname> list(Long chatId) {
        HashMap<Integer, Nickname> chatMembers = members.get(chatId);
        if (chatMembers != null) {
            return chatMembers.values();
        }
        return null;
    }

    public void remove(Long chatId, Integer userId) {
        HashMap<Integer, Nickname> chatMembers = members.get(chatId);
        if (chatMembers != null) {
            chatMembers.remove(userId);
        }
    }

    public Nickname randomAt(Long chatId) {
        Collection<Nickname> at = list(chatId);
        int rPos = RDBResource.RNG.nextInt(at.size());
        return (Nickname)at.toArray()[rPos];
    }

}
