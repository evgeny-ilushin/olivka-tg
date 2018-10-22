package irc.tula.tg.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "text",
        "isUserName"
})
public class Nickname {
    @JsonProperty("text")
    private String text;

    @JsonProperty("isUserName")
    private boolean isUserName;

    @Override
    public String toString() {
        return (isUserName ? NewWorld.NICK_PREFIX : "") + text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Nickname nickname = (Nickname) o;
        return Objects.equals(text, nickname.text);
    }

    @Override
    public int hashCode() {
        return Objects.hash(text);
    }
}
