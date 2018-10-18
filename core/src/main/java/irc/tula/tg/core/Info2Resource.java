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

@Slf4j
public class Info2Resource {
    @Getter
    @Setter(AccessLevel.PROTECTED)
    private final String fullPath;

    @Getter
    private boolean availabe = false;

    @Getter @Setter(AccessLevel.PROTECTED)
    private final List<Info2Record> data = new ArrayList<Info2Record>();

    public int size() { return data.size(); }

    public Info2Resource(String fullPath) {
        this.fullPath = fullPath;
        reload();
    }

    private void reload() {
        try (Stream<String> stream = Files.lines(Paths.get(fullPath), Charset.forName(Cave.getLocale()))) {
            stream.forEach(line -> {
                if (StringUtils.isNotBlank(line)) {
                    Optional<Info2Record> item = Info2Record.fromSource(line);
                    if (item.isPresent()) {
                        data.add(item.get());
                    }

                }
            });
            availabe = size() > 0;
        } catch (Exception ex) {
            log.error("RDB error \"{}\" - {}", fullPath, ex);
        }
    }

    public String toJSON() {
        return "[]";
    }
}

@Data
@Slf4j
@AllArgsConstructor
class Info2Record {
    private String pattern;
    private String value;

    public boolean matches(String text) {
        char c = 0;
        int index = 0;
        boolean match = true;

        if (text == null || text.length() < 1) {
            return false;
        }

        for (char p: text.toCharArray()) {
            switch (c) {
                case '?':
                    c = text.charAt(index++);
                    break;
                case '*':
                    break;
            }
        }

        do {
            c = text.charAt(index++);

            if (index >= text.length()) {
                c = 0;
                break;
            }
        } while (c != 0);
        return false;
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