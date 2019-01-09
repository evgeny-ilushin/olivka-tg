package irc.tula.tg.core.entity;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.NoArgsConstructor;

import java.util.Calendar;
import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "day",
        "month",
        "year",
        "text",
        "nick",
        "ts"
})

@NoArgsConstructor
public class DateItem {

    @JsonProperty("id")
    private Integer id;
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

}