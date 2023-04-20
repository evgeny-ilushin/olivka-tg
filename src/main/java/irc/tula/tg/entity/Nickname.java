package irc.tula.tg.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import irc.tula.tg.NewWorld;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Objects;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "text",
        "isUserName",
        "lastSeen"
})
public class Nickname {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("text")
    private String text;

    @JsonProperty("isUserName")
    private boolean isUserName;

    @JsonProperty("totalMessages")
    private Integer totalMessages;

    @JsonProperty("isBot")
    private boolean isBot;

    @JsonProperty("lastSeen")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastSeen;

    public Nickname(Long id, String text, boolean isUserName) {
        this.id = id;
        this.text = text;
        this.isUserName = isUserName;
        lastSeen = new Date();
    }

    public void notice() { lastSeen = new Date(); }

    @Override
    public String toString() {
        return (isUserName ? NewWorld.NICK_PREFIX : "") + text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Nickname nickname = (Nickname) o;
        return isUserName == nickname.isUserName &&
                Objects.equals(id, nickname.id) &&
                Objects.equals(text, nickname.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, text, isUserName);
    }
}
