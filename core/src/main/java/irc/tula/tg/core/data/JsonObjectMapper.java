package irc.tula.tg.core.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import irc.tula.tg.core.BotConfig;
import irc.tula.tg.core.NewWorld;
import lombok.Getter;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

public class JsonObjectMapper implements MyObjectMapper {
    private final String rootPath;

    @Getter
    private static final ObjectMapper mapper = new ObjectMapper();

    public JsonObjectMapper(String rootPath) {
        this.rootPath = rootPath;
    }

    public static Optional<BotConfig> readConfig(String filePath) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(filePath)));
            return Optional.of(mapper.readValue(content, BotConfig.class));
        } catch (Exception ex) {
            ex.printStackTrace();
            return Optional.empty();
        }
    }

    public static String dumpConfig(BotConfig config) throws JsonProcessingException {
            return mapper.writeValueAsString(config);
    }

    public Optional<Object> read(String fileOrString) {
        return Optional.empty();
    }

    public <T> T read(String filePath, Class<T> valueType) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(rootPath + NewWorld.PATH_SEPARATOR + filePath)));
            return mapper.readValue(content, valueType);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public void write(String filePath, Object object) {
        try {
            String dump = mapper.writeValueAsString(object);
            Files.write(Paths.get(rootPath + NewWorld.PATH_SEPARATOR + filePath), dump.getBytes(), StandardOpenOption.CREATE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
