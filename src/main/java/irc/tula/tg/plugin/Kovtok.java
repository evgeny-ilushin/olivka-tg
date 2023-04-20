package irc.tula.tg.plugin;

import irc.tula.tg.ChannelBot;
import irc.tula.tg.entity.IncomingMessage;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

public class Kovtok implements Plugin {

    private static final int KOVTOK_HOUR = 18;

    public static final String PLUGIN_NAME_U = "kovtok";
    public static final String PLUGIN_NAME_R = "glotok";
    public static final String PLUGIN_NAME = PLUGIN_NAME_U;

    private static long unixTime() {
        return System.currentTimeMillis() / 1000L;
    }

    public static String infoU() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));
        return infoU(now);
    }

    public static String infoU(ZonedDateTime now) {
        ZonedDateTime kovtokTime = now.withHour(KOVTOK_HOUR).withMinute(0);
        long diff = ChronoUnit.SECONDS.between(now, kovtokTime);
        return infoU(diff);
    }

    public static String infoU(long tDiff) {
        String ts;

        if (tDiff < 0) {
            return "вже давно пора";
        }

        long hDiff = tDiff / 3600L;
        long mDiff = (tDiff - hDiff * 3600L) / 60L;

        if (hDiff > 0) {
            ts = String.format("%d %s %d %s", hDiff,
                    ((hDiff == 1 || hDiff == 21) ? "година" : ((hDiff == 2 || hDiff == 3 || hDiff == 4) ? "години"
                            : "годин")),
                    mDiff,
                    ((mDiff == 1 || (mDiff % 10 == 1 && mDiff > 11)) ? "хвилина" : ((mDiff % 10 == 2 || mDiff % 10 == 3 ||
                            mDiff % 10 == 4) ? "хвилини" : "хвилин")));
        } else {
            ts = String.format("%d %s", mDiff,
                    ((mDiff == 1 || (mDiff % 10 == 1 && mDiff > 11)) ? "хвилина" : ((mDiff % 10 == 2 || mDiff % 10 == 3 ||
                            mDiff % 10 == 4) ? "хвилини" : "хвилин")));
        }

        return "до ковтка залишилось " + ts;
    }

    public static String infoR() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));
        return infoR(now);
    }

    public static String infoR(ZonedDateTime now) {
        ZonedDateTime kovtokTime = now.withHour(KOVTOK_HOUR).withMinute(0);
        long diff = ChronoUnit.SECONDS.between(now, kovtokTime);
        return infoR(diff);
    }

    public static String infoR(long tDiff) {
        String ts;

        if (tDiff < 0) {
            return "уже давно пора";
        }

        long hDiff = tDiff / 3600L;
        long mDiff = (tDiff - hDiff * 3600L) / 60L;

        if (hDiff > 0) {
            ts = String.format("%d %s %d %s", hDiff,
                    ((hDiff == 1 || hDiff == 21) ? "час" : ((hDiff == 2 || hDiff == 3 || hDiff == 4) ? "часа"
                            : "часов")),
                    mDiff,
                    ((mDiff == 1 || (mDiff % 10 == 1 && mDiff > 11)) ? "минута" : ((mDiff % 10 == 2 || mDiff % 10 == 3 ||
                            mDiff % 10 == 4) ? "минуты" : "минут")));
        } else {
            ts = String.format("%d %s", mDiff,
                    ((mDiff == 1 || (mDiff % 10 == 1 && mDiff > 11)) ? "минута" : ((mDiff % 10 == 2 || mDiff % 10 == 3 ||
                            mDiff % 10 == 4) ? "минуты" : "минут")));
        }

        return "до глотка осталось " + ts;
    }

    public static void main(String[] args) {
        System.out.println(infoR());
        System.out.println(infoU());
    }

    @Override
    public String getName() {
        return PLUGIN_NAME;
    }

    @Override
    public List<String> getNames() {
        return Arrays.asList(PLUGIN_NAME_U, PLUGIN_NAME_R);
    }

    @Override
    public void initialize(ChannelBot bot) {
    }

    @Override
    public boolean process(ChannelBot bot, IncomingMessage msg, String pluginName, String[] params) {
        String info;
        if (PLUGIN_NAME_R.equalsIgnoreCase(pluginName)) {
            info = infoR();
        } else {
            info = infoU();
        }
        bot.sayOnChannel(msg.getChatId(), msg.getNickName() + ", " + info);
        return true;
    }

    @Override
    public boolean processCommand(ChannelBot bot, String cmd, String params, IncomingMessage msg, String pluginName) {
        return false;
    }

    @Override
    public void release(ChannelBot bot) {

    }
}
