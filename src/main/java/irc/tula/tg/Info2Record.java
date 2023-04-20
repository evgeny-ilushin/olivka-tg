package irc.tula.tg;

import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Data
@Slf4j
@AllArgsConstructor
class Info2Record {
    private String pattern;
    private String value;

    public boolean matches(String text) {
        return MatchWild.match_wild(pattern, text) == 1;
    }

    public static Optional<Info2Record>fromSource(String source) {
        try {
            int sep = source.indexOf(" ");
            return Optional.of(new Info2Record(source.substring(0, sep).trim(), source.substring(sep).trim()));
        } catch (Exception ex) {
            ex.printStackTrace();
            return Optional.empty();
        }
    }
}