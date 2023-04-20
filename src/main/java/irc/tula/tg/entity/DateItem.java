package irc.tula.tg.entity;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.NoArgsConstructor;

import java.util.Calendar;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "sid",
        "day",
        "month",
        "year",
        "text",
        "nick",
        "ts",
        "id"
})

@NoArgsConstructor
public class DateItem implements Comparable<DateItem> {

    @JsonProperty("id")
    private Integer id;
    @JsonProperty("sid")
    private String sid;
    @JsonProperty("day")
    private Integer day;
    @JsonProperty("month")
    private Integer month;
    @JsonProperty("year")
    private Integer year;
    @JsonProperty("text")
    private String text;
    @JsonProperty("nick")
    private String nick;
    @JsonProperty("ts")
    private Long ts;

    public static DateItem addNew(Date date, String text, Nickname nickName) {
        DateItem res = new DateItem();
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        res.setNick(nickName.toString());
        res.setText(text);
        res.setTs((new Date().getTime())/1000L);
        res.setYear(cal.get(Calendar.YEAR));
        res.setDay(cal.get(Calendar.DAY_OF_MONTH));
        res.setMonth(cal.get(Calendar.MONTH)+1);
        res.setSid();
        return res;
    }

    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(Integer id) {
        this.id = id;
    }

    @JsonProperty("sid")
    public String getSid() {
        return sid;
    }

    @JsonProperty("sid")
    public void setSid(String sid) {
        this.sid = sid;
    }
    public void setSid() {
        this.sid = toSortableString();
    }

    @JsonProperty("day")
    public Integer getDay() {
        return day;
    }

    @JsonProperty("day")
    public void setDay(Integer day) {
        this.day = day;
    }

    @JsonProperty("month")
    public Integer getMonth() {
        return month;
    }

    @JsonProperty("month")
    public void setMonth(Integer month) {
        this.month = month;
    }

    @JsonProperty("year")
    public Integer getYear() {
        return year;
    }

    @JsonProperty("year")
    public void setYear(Integer year) {
        this.year = year;
    }

    @JsonProperty("text")
    public String getText() {
        return text;
    }

    @JsonProperty("text")
    public void setText(String text) {
        this.text = text;
    }

    @JsonProperty("nick")
    public String getNick() {
        return nick;
    }

    @JsonProperty("nick")
    public void setNick(String nick) {
        this.nick = nick;
    }

    @JsonProperty("ts")
    public Long getTs() {
        return ts;
    }

    @JsonProperty("ts")
    public void setTs(Long ts) {
        this.ts = ts;
    }

    @Override
    public int compareTo(DateItem o) {
        return (sid = toSortableString()).compareTo(o.toSortableString());
    }

    public String toSortableString() {
        Integer n_year = (year == null || year.equals(0))? 1970 : year;
        //return String.format("%04d_%02d_%02d", n_year, month, day);
        return String.format("%02d_%02d_%04d", month, day, n_year);
    }
}