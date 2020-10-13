package irc.tula.tg.core.plugin;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.HashMap;

public class Qify {
    private static final int MIN_WORD_LENGTH = 3;

    public static String text(String text) {
        try {
            StringBuilder res = new StringBuilder();
            String word = null;
            for (int i = 0; i < text.length(); i++) {
                String cs = text.substring(i, i + 1);
                if (StringUtils.isAlpha(cs)) {
                    if (word == null) {
                        word = "";
                    }
                    word += cs;
                } else {
                    if (word != null) {
                        word = word(word);
                        res.append(word);
                        word = null;
                    }
                    res.append(cs);
                }
            }
            if (word != null) {
                word = word(word);
                res.append(word);
            }
            return res.toString();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return text;
    }

    public static String word(String word) {
        String source = word;

        if (word == null || word.length() < MIN_WORD_LENGTH) {
            return word;
        }

        word = word.trim();
        String lcWord = word.toLowerCase().trim();
        Boolean isUcWord = StringUtils.isAllUpperCase(""+word.charAt(0));

        String vowels = "аеёиоуыэюя";
        HashMap<Character, Character> rules = new HashMap<Character, Character>();
        rules.put('а', 'я');
        rules.put('о', 'ё');
        rules.put('у', 'ю');
        rules.put('ы', 'и');
        rules.put('э', 'е');

        for (char letter : word.toCharArray()) {
            boolean isUc = Character.isUpperCase(letter);
            char lcLetter = Character.toLowerCase(letter);
            if (vowels.indexOf(lcLetter) != -1) {
                if (rules.containsKey(lcLetter)) {
                    word = (isUc? Character.toUpperCase(rules.get(lcLetter)) : rules.get(lcLetter)) + word.substring(1);
                }
                break;
            } else {
                word = word.substring(1);
            }
        }
        return word.isEmpty() ? source : ((isUcWord?"Х":"х") + "у" + word);
    }

    @Test
    public static void test1() {
        System.out.println(text("Привет, ты и дела, и еще Тектоническая плита"));
    }

    public static void main(String[] args) {
        /*
        test1();
        if (1 == 1) {
            return;
        }
        */
        String text = String.join(" ", args);
        if (StringUtils.isNotBlank(text)) {
            String qtext = text(text);
            if (!text.equals(qtext)) {
                System.out.println(qtext);
            } else {
                System.err.println("unprocessable");
            }
        } else {
            System.err.println("Usage: Qify <TEXT>");
        }
    }
}
