package irc.tula.tg.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.beans.Transient;
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

    private String encoding;

    private List<String> admins;
    private List<String> names;
    private List<String> ignore;

    private boolean debug;
    private boolean runTests = false;

    private Boolean alwaysShowTyping = true;

    public boolean isAdmin(String nick) {
        return (admins != null && admins.size() > 0 && admins.contains(nick));
    }
    public boolean isIgnored(String nick) {
        return (ignore != null && ignore.size() > 0 && ignore.contains(nick));
    }

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
        return new BotConfig("/home/mybot", "123123123:454564", Cave.getEncoding(), Arrays.asList("@murzambek", "@zloy"), Arrays.asList("bot", "MyBot", "@my_bot_nick"), Arrays.asList("123"), true, false, true);
    }

    public String asTable() {
        return "{" +
                "baseDir='" + baseDir + '\'' +
                ", token='" + token + '\'' +
                ", encoding='" + encoding + '\'' +
                ", admins=" + admins +
                ", names=" + names +
                ", ignore=" + ignore +
                ", debug=" + debug +
                ", runTests=" + runTests +
                ", alwaysShowTyping=" + alwaysShowTyping +
                '}';
    }
}
