package irc.tula.tg.core.entity;

import lombok.*;

import java.util.Date;

@Data
@RequiredArgsConstructor
public class IncomingMessage {
    @NonNull Long chatId;
    @NonNull Nickname nickName;
    @NonNull String text;
    @NonNull boolean isPersonal;
    @NonNull boolean isAdminMessage;

    final Date ts = new Date();

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
