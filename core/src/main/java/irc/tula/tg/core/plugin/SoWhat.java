package irc.tula.tg.core.plugin;

import irc.tula.tg.core.ChannelBot;
import irc.tula.tg.core.entity.IncomingMessage;
import irc.tula.tg.core.entity.Nickname;
import irc.tula.tg.core.entity.DateInfoCollection;
import irc.tula.tg.core.entity.DateItem;
import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.*;

public class SoWhat implements Plugin {

    public static final String PLUGIN_NAME = "sowhat";
    public static final String PLUGIN_MODE_FULL = PLUGIN_NAME + "_full";
    public static final String PLUGIN_MODE_CUSTOM = PLUGIN_NAME + "_custom";

    public static final String[] PLUGIN_ALIASES = { PLUGIN_NAME, PLUGIN_MODE_FULL, PLUGIN_MODE_CUSTOM };

    public static final String SOWHAT_DB = "sowhat.json";

    public static final String SOWHAT_RDB = "dateprefix1";
    public static final String SOWHAT_RDB_FEW = "dateprefix2";
    public static final String SOWHAT_RDB_NONE = "dateprefix3";

    //private static final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    private static final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");


    private DateInfoCollection database;
    private boolean loaded = false;

    public String getName() { return "sowhat"; }

    public List<String> getNames() {
        return Arrays.asList(PLUGIN_ALIASES);
    }

    public DateInfoCollection getDatabase() {
        return database;
    }

    public void setDatabase(DateInfoCollection database) {
        this.database = database;
    }

    @Override
    public void initialize(ChannelBot bot) {

        if (!loaded) {
            database = bot.getMapper().read(SOWHAT_DB, DateInfoCollection.class);
            Collections.sort(database.getItems());
            //bot.getMapper().write(SOWHAT_DB+".new.json", database);

            loaded = true;
        }
    }

    @Override
    public void release(ChannelBot bot) {}

    @Override
    public boolean process(ChannelBot bot, IncomingMessage msg, String pluginName) {
        final Calendar now = Calendar.getInstance();
        List<DateItem> today = new ArrayList<>();
        boolean personal = false;

        if (PLUGIN_MODE_CUSTOM.equalsIgnoreCase(pluginName)) {
            personal = true; // Only custom records
        }

        // Collect matches
        for (DateItem di : database.getItems()) {
            if (now.get(Calendar.DAY_OF_MONTH) == di.getDay()
                && (now.get(Calendar.MONTH)+1) == di.getMonth()) {
                if (!personal) {
                    today.add(di);
                } else {
                    if (StringUtils.isNotBlank(di.getNick()) && !"n/a".equalsIgnoreCase(di.getNick()) && di.getTs() != null) {
                        today.add(di);
                    }
                }
            }
        }

        // Filer out dupes
        HashSet<String> all = new HashSet<>();
        for (DateItem i : today) {
            if (all.contains(i.getText())) {
                today.remove(i);
            } else {
                all.add(i.getText());
            }
        }

        if (today.size() <= 0) {
            // Nothing
            bot.answerRdb(msg, SOWHAT_RDB_NONE);
        } else {
            if (today.size() <= 4) {
                // Not too much
                bot.answerRdb(msg, SOWHAT_RDB_FEW);
            } else {
                // Normal
                bot.answerRdb(msg, SOWHAT_RDB);
            }

            int maxNdx = 5; // Load 10 or less

            if (PLUGIN_MODE_FULL.equalsIgnoreCase(pluginName)) {
                maxNdx = 10000; // Infinity : )
            }

            int ndx = 1;
            for (DateItem i : today) {
                if (personal) {
                    Date ts = new Date(1000L * i.getTs());
                    String l = "" + (ndx++) + ". <i>" + i.getText() + "</i> (" + i.getNick() + ", " + format.format(ts) + ")";
                    bot.sayOnChannel(msg.getChatId(), l);
                } else {
                    String l = "" + (ndx++) + ". <i>" + i.getText() + "</i>";
                    bot.sayOnChannel(msg.getChatId(), l);
                }
                if (ndx > maxNdx) {
                    if (today.size() > maxNdx) {
                        String tail = "...<b>" + (today.size() - maxNdx) + "</b> more";
                        bot.sayOnChannel(msg.getChatId(), tail);
                    }
                    break;
                }
            }
        }

        return false;
    }

    @Override
    public boolean processCommand(ChannelBot bot, String cmd, String params, IncomingMessage msg, String pluginName) {
        boolean res = false;

        if (msg.isPersonal() && "adddate".equalsIgnoreCase(cmd)) {
            String[] result = params.trim().split(" ", 2);
            if (result == null || result.length < 2) {
                bot.answerDonno(msg);
                return res = false;
            }

            String sdate = result[0];
            String text = result[1];
            Date date = null;

            try {
                date = format.parse(sdate);
            } catch (Exception ex) {
                ex.printStackTrace();
                bot.answerDonno(msg);
                return res = false;
            }

            if (date == null || StringUtils.isBlank(text)) {
                bot.answerDonno(msg);
                return res = false;
            }

            try {
                DateItem ndi = DateItem.addNew(date, text, msg.getNickName());
                database.getItems().add(ndi);
                bot.getMapper().write(SOWHAT_DB, database);
                res = true;

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }

        return res;
    }
}
