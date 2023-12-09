package irc.tula.tg.plugin;

import irc.tula.tg.ChannelBot;
import irc.tula.tg.entity.IncomingMessage;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

public class Glotok implements Plugin {

    private static final int KOVTOK_HOUR_12 = 12;
    private static final int KOVTOK_HOUR_14 = 14;

    private static final int KOVTOK_OFF = 22;

    public static final String PLUGIN_NAME_U = "glotok";
    public static final String PLUGIN_NAME_R = "kovtok";
    public static final String PLUGIN_NAME = PLUGIN_NAME_U;

    private static long unixTime() {
        return System.currentTimeMillis() / 1000L;
    }

    public static String infoR() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));
        return infoR(now);
    }

    public static String infoR(ZonedDateTime now) {
        int kHour = KOVTOK_HOUR_12;
        switch (now.getDayOfWeek()) {
            case MONDAY:
            case TUESDAY:
            case WEDNESDAY:
            case THURSDAY:
            case FRIDAY:
                kHour = KOVTOK_HOUR_14;
        };

        ZonedDateTime kovtokTime = now.withHour(kHour).withMinute(0);
        ZonedDateTime kovtokOff = now.withHour(KOVTOK_OFF).withMinute(0);
        long tPre = ChronoUnit.SECONDS.between(now, kovtokTime);
        long tPost = ChronoUnit.SECONDS.between(now, kovtokOff);

        String ts;

        if (tPre < 0) {
            if (tPost > 0) {
                return yes_left(tPost);
            } else {
                return closed();
            }
        } else {
            return no_wait(tPre);
        }
    }

    public static String yes_left(long time) {
        String ts = toTime(time);
        boolean oneEnd = ts.endsWith("1 минута") && !ts.contains("час");
        return (oneEnd? "до закрытия осталась " : "до закрытия осталось ") + ts;
    }

    public static String no_wait(long time) {
        String ts = toTime(time);
        boolean oneEnd = ts.endsWith("1 минута") && !ts.contains("час");
        return (oneEnd? "до глотка осталась " : "до глотка осталось ") + ts;
    }

    public static String closed() {
        return "всё закрыто";
    }

    public static String toTime(long tPre) {
        long hDiff = tPre / 3600L;
        long mDiff = (tPre - hDiff * 3600L) / 60L;

        if (hDiff > 0) {
            return String.format("%d %s %d %s", hDiff,
                    ((hDiff == 1 || hDiff == 21) ? "час" : ((hDiff == 2 || hDiff == 3 || hDiff == 4) ? "часа"
                            : "часов")),
                    mDiff,
                    ((mDiff == 1 || (mDiff % 10 == 1 && mDiff > 11)) ? "минута" : ((mDiff % 10 == 2 || mDiff % 10 == 3 ||
                            mDiff % 10 == 4) ? "минуты" : "минут")));
        } else {
            return String.format("%d %s", mDiff,
                    ((mDiff == 1 || (mDiff % 10 == 1 && mDiff > 11)) ? "минута" : (((mDiff % 10 == 2 || mDiff % 10 == 3 ||
                            mDiff % 10 == 4) && (mDiff < 5 || mDiff > 20)) ? "минуты" : "минут")));
        }
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
    public boolean process(ChannelBot bot, IncomingMessage msg, String pluginName, String[] params) {
        String info = infoR();
        bot.sayOnChannel(msg.getChatId(), msg.getNickName() + ", " + info);
        return true;
    }

    public static void t(ZonedDateTime t1) {
        System.out.println("" + t1 + " -> " + infoR(t1));
    }

    public static void main(String[] args) {
        System.out.println(infoR());
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Moscow"));
        ZonedDateTime t1 = now.withHour(11).withMinute(37);
        t(t1);
        t1 = now.withHour(8).withMinute(2);
        t(t1);
        t1 = now.withHour(18).withMinute(25);
        t(t1);
        t1 = now.withHour(21).withMinute(19);
        t(t1);
        t1 = now.withHour(21).withMinute(59);
        t(t1);
        t1 = now.withHour(21).withMinute(58);
        t(t1);
    }

}
