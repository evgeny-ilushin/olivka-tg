package irc.tula.tg.core;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BotConfig {
    private static final String LOG_FOLDER = "log";
    private static final String DATA_FOLDER = "data";
    private static final String SCRIPTS_FOLDER = "scripts";

    private String baseDir;

    private String token;

    private List<String> names;

    private boolean debug;

    @Transient
    public String getDataDirName() {
        return baseDir + NewWorld.PATH_SEPARATOR + DATA_FOLDER;
    }

    @Transient
    public String getDataDir(String nameOnly) {
        return baseDir + NewWorld.PATH_SEPARATOR + DATA_FOLDER + NewWorld.PATH_SEPARATOR + nameOnly;
    }

    @Transient
    public String getLogDir(String nameOnly) {
        return baseDir + NewWorld.PATH_SEPARATOR + LOG_FOLDER + NewWorld.PATH_SEPARATOR + nameOnly;
    }

    @Transient
    public String getScriptDir(String nameOnly) {
        return baseDir + NewWorld.PATH_SEPARATOR + SCRIPTS_FOLDER + NewWorld.PATH_SEPARATOR + nameOnly;
    }

    @Transient
    public static BotConfig getSample() {
        return new BotConfig("/home/mybot", "123123123:454564", Arrays.asList("bot", "MyBot", "@my_bot_nick"), true);
    }
}
