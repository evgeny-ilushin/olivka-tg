package irc.tula.tg.core;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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