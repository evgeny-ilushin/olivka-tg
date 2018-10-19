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
    private final String directoryPath;

    @Getter
    @Setter(AccessLevel.PROTECTED)
    private final String fileName;

    @Getter
    private boolean availabe = false;

    @Getter @Setter(AccessLevel.PROTECTED)
    private final List<Info2Record> data = new ArrayList<Info2Record>();

    public int size() { return data.size(); }

    public Info2Resource(String directoryPath, String fileName) {
        this.directoryPath = directoryPath;
        this.fileName = fileName;
        reload();
    }

    public Optional<Info2Record> firstMatch(String text) {
        String text2 = MatchWild.preparePattern(text);
        for (irc.tula.tg.core.Info2Record r : data) {
            if (r.matches(text2)) {
                return Optional.of(r);
            }
        }
        return Optional.empty();
    }

    private void reload() {
        String fullPath = directoryPath + NewWorld.PATH_SEPARATOR + fileName;
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

