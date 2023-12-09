package irc.tula.tg.plugin;

import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import irc.tula.tg.ChannelBot;
import irc.tula.tg.entity.IncomingMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
public class Wiki implements Plugin {
    private static final String[] PREFS = { "wtf ", "wiki " };
    private static final int MAX_WIKI = 500;
    private static final boolean tryRussianFirst = true;
    private static final String DOTES = "...";

    @Override
    public String getName() {
        return "wiki";
    }

    @Override
    public List<String> getNames() {
        return null;
    }

    @Override
    public void initialize(ChannelBot bot) {

    }

    @Override
    public boolean process(ChannelBot bot, IncomingMessage msg, String pluginName, String[] params) {
        String info;
        final String[] answer = new String[1];
        Arrays.stream(PREFS).forEach(e -> {
            if (msg.getText().startsWith(e)) {
                answer[0] = msg.getText().substring(e.length()).trim();
            }
        });
        if (answer[0] != null) {
            WikiHelper wh = new WikiHelper(answer[0]);
            if (wh.getExtractText() != null) {
                bot.sayOnChannel(msg.getChatId(), msg.getNickName() + ", " + limitText(wh.getExtractText(), MAX_WIKI));
                return true;
            } else {
                bot.answerDonno(msg);
            }
        } else {
            //bot.answerDonno(msg);
        }
        return false;
    }

    private Object limitText(String text, int maxLengh) {
        if (text == null || text.length() < maxLengh) {
            return text;
        }
        return text.substring(0, maxLengh) + DOTES;
    }

    @Override
    public boolean processCommand(ChannelBot bot, String cmd, String params, IncomingMessage msg, String pluginName) {
        return false;
    }

    @Override
    public void release(ChannelBot bot) {

    }
}
class WikiHelper {
    private static final boolean tryRussianFirst = true;
    final String BASE_URL = "https://en.wikipedia.org/api/rest_v1/page/summary/";
    final String BASE_URL_R = "https://ru.wikipedia.org/api/rest_v1/page/summary/";
    String subject=null;
    String displayTitle="";
    String imageURL="";
    String extractText="";

    public WikiHelper(String subject)
    {
        this.subject=subject;
        getData();
    }

    private void getData() {
        if (tryRussianFirst) {
            if (containsCyrilic(subject)) {
                getData(BASE_URL_R);
                if (extractText == null || StringUtils.isBlank(extractText)) {
                    getData(BASE_URL);
                }
            }
            else {
                getData(urlFor(subject));
            }
        }
        else {
            getData(urlFor(subject));
        }
    }
    private void getData(String wikiUrl) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(wikiUrl + subject)
                .get()
                .build();
        try {
            Response response=client.newCall(request).execute();
            String data = response.body().string();
            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject)parser.parse(data);
            displayTitle= (String) jsonObject.get("displaytitle");
            JSONObject jsonObjectOriginalImage = (JSONObject) jsonObject.get("originalimage");
            if (jsonObjectOriginalImage != null) {
                imageURL = (String) jsonObjectOriginalImage.get("source");
            }
            extractText = (String)jsonObject.get("extract");
        }
        catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private String urlFor(String text) {
        for (int i = 0; i < text.length(); i++) {
            if (Character.UnicodeBlock.of(text.charAt(i)).equals(Character.UnicodeBlock.CYRILLIC)) {
                return BASE_URL_R;
            }
        }
        return BASE_URL;
    }

    private Boolean containsCyrilic_ГОВНО(String text) {
        return text.chars().allMatch(e -> !Character.UnicodeBlock.of(e).equals(Character.UnicodeBlock.CYRILLIC));
    }

    private Boolean containsCyrilic(String text) {
        final Pattern pattern = Pattern.compile(
                "[" +                   //начало списка допустимых символов
                        "а-яА-ЯёЁ" +    //буквы русского алфавита
                        "\\d" +         //цифры
                        "\\s" +         //знаки-разделители (пробел, табуляция и т.д.)
                        "\\p{Punct}" +  //знаки пунктуации
                        "]" +                   //конец списка допустимых символов
                        "*");
        return pattern.matcher(text).matches();
    }

    public String getDisplayTitle() {
        return displayTitle;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getExtractText() {
        return extractText;
    }
}