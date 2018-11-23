package irc.tula.tg.core.plugin;

import irc.tula.tg.core.ChannelBot;
import irc.tula.tg.core.Nickname;
import irc.tula.tg.core.entity.DateInfoCollection;
import irc.tula.tg.core.entity.DateItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SoWhat implements Plugin {

    public static final String SOWHAT_DB = "sowhat.json";

    public static final String SOWHAT_RDB = "dateprefix1";
    public static final String SOWHAT_RDB_FEW = "dateprefix2";
    public static final String SOWHAT_RDB_NONE = "dateprefix3";


    private DateInfoCollection database;

    public String getName() { return "sowhat"; }

    public DateInfoCollection getDatabase() {
        return database;
    }

    public void setDatabase(DateInfoCollection database) {
        this.database = database;
    }

    @Override
    public void initialize(ChannelBot bot) {
        database = bot.getMapper().read(SOWHAT_DB, DateInfoCollection.class);
    }

    @Override
    public void release(ChannelBot bot) {}

    @Override
    public boolean process(ChannelBot bot, Long chatId, Nickname nickName, String text) {
        final Calendar now = Calendar.getInstance();
        List<DateItem> today = new ArrayList<>();

        for (DateItem di : database.getItems()) {
            if (now.get(Calendar.DAY_OF_MONTH) == di.getDay() &&
                    now.get(Calendar.MONTH) == di.getMonth()) {
                today.add(di);
            }
        }

        if (today.size() <= 0) {
            bot.answerRdb(chatId, nickName, SOWHAT_RDB_NONE);
        } else {
            if (today.size() <= 4) {
                bot.answerRdb(chatId, nickName, SOWHAT_RDB_FEW);
            } else {
                bot.answerRdb(chatId, nickName, SOWHAT_RDB);
            }

            StringBuilder dates = new StringBuilder();
            int ndx = 1;
            for (DateItem i : today) {
                //if (dates.length() > 0 ) {
                    //dates.append("</b>");
                    String l = "" + (ndx++) + ". " + i.getText();
                    bot.sayOnChannel(chatId, l);
                //}
                //dates.append("" + (ndx++) + ". " + i.getText());
            }
            //bot.sayOnChannel(chatId, dates.toString());
        }

        return false;
    }
}
