package irc.tula.tg.entity;

import com.pengrad.telegrambot.model.Message;
import lombok.*;

import java.util.Date;

@Data
public class IncomingMessage {
    final @NonNull Long chatId;
    final @NonNull Nickname nickName;
    final @NonNull String text;
    final @NonNull boolean isPersonal;
    final @NonNull boolean isAdminMessage;
    boolean wasTrimmedToParams = false;
    final Message originalMessage;

    final Date ts = new Date();

    public IncomingMessage(@NonNull Long chatId, @NonNull Nickname nickName, @NonNull String text, @NonNull boolean isPersonal, @NonNull boolean isAdminMessage, Message originalMessage) {
        this.chatId = chatId;
        this.nickName = nickName;
        this.text = text;
        this.isPersonal = isPersonal;
        this.isAdminMessage = isAdminMessage;
        this.originalMessage = originalMessage;
    }

    @Override
    public String toString() {
        return "IncomingMessage{" +
                "chatId=" + chatId +
                ", nickName=" + nickName +
                ", text='" + text + '\'' +
                ", isPersonal=" + isPersonal +
                ", isAdminMessage=" + isAdminMessage +
                ", ts=" + ts +
                '}';
    }
}
