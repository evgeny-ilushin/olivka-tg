package irc.tula.tg.core;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Nickname {
    private String text;
    private boolean isUserName;

    @Override
    public String toString() {
        return (isUserName ? NewWorld.NICK_PREFIX : "") + text;
    }
}
