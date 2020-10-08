package irc.tula.tg.core.plugin;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;

public class Qify {
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
        word = word.toLowerCase().trim();

        String vowels = "аеёиоуыэюя";
        HashMap<Character, Character> rules = new HashMap<Character, Character>();
        rules.put('а', 'я');
        rules.put('о', 'ё');
        rules.put('у', 'ю');
        rules.put('ы', 'и');
        rules.put('э', 'е');

        for (char letter : word.toCharArray()) {
            if (vowels.indexOf(letter) != -1) {
                if (rules.containsKey(letter)) {
                    word = rules.get(letter) + word.substring(1);
                }
                break;
            } else {
                word = word.substring(1);
            }
        }
        return word.isEmpty() ? source : "ху" + word;
    }

    public static void main(String[] args) {
        String s1 = text("литосферная плита!");
        System.out.println(s1);
    }
}
