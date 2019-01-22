package irc.tula.tg.core;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
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
public class RDBResource {
    protected static final Random RNG = new Random(System.currentTimeMillis());

    @Getter @Setter(AccessLevel.PROTECTED)
    private final String fullPath;

    @Getter
    private boolean availabe = false;

    @Getter @Setter(AccessLevel.PROTECTED)
    private Integer delay = 0;

    @Getter @Setter(AccessLevel.PROTECTED)
    private final List<String> data = new ArrayList<String>();

    public int size() { return data.size(); }

    public RDBResource(String fullPath) {
        this.fullPath = fullPath;
        reload();
    }

    private void reload() {
        try (Stream<String> stream = Files.lines(Paths.get(fullPath), Charset.forName(Cave.getLocale()))) {
            stream.forEach(line -> {
                if (StringUtils.isNotBlank(line)) {
                    data.add(line.trim());
                }
            });
            if (data.size() > 0) {
                // Header
                String head = data.get(0);
                if (StringUtils.isNotBlank(head) && head.contains(Cave.LINE_SEPARATOR)) {
                    try {
                        delay = Integer.parseInt(head.split(Pattern.quote(Cave.LINE_SEPARATOR))[1]);
                    } catch (Exception ex) {
                        log.error("RDB header error \"{}\" - {}", head, ex);
                        delay = Cave.DEFAULT_REPLY_DELAY;
                    }
                }
                data.remove(0);
                availabe = size() > 0;
            }
        } catch (Exception ex) {
            log.error("RDB error \"{}\" - {}", fullPath, ex);
        }
    }

    public Optional<String> next() {
        if (availabe) {
            return Optional.of(data.get(RNG.nextInt(data.size()-1)));
        } else {
            return Optional.empty();
        }
    }

    @Deprecated
    public String nextString() {
        if (availabe) {
            String preRes = data.get(RNG.nextInt(data.size()));
            return preRes;
        }
        return null;
    }

    public String toJSON() {
        StringBuilder b = new StringBuilder("{ \"size\": \": " + size() + ", \"delay\":" + delay + ", \"items\":[");
        data.forEach(i -> {
            b.append("\"" + i + "\", ");
        });
        if (b.charAt(b.length()-1) == ' ' && b.charAt(b.length()-2) == ',') {
            b.delete(b.length()-2, b.length()-1);
        }
        b.append("]}");
        return b.toString();
    }
}
