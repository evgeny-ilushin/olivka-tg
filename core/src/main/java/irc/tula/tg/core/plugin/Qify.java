package irc.tula.tg.core.plugin;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Random;

public class Qify {
    private static final int MIN_WORD_LENGTH = 3;

    private static final int MIN_SHORT_WORD_LENGTH = 2;
    private static final int MAX_SHORT_WORD_LENGTH = 3;

    private static final String SHORT_WORD_PRE_1 = "хуе";

    private static final int MIN_WORD_LENGTH_1V = 5;

    private static final String vowels = "аеёиоуыэюя";
    private static final HashMap<Character, Character> rules = new HashMap<Character, Character>();
    static {
        rules.put('а','я');
        rules.put('о','ё');
        rules.put('у','ю');
        rules.put('ы','и');
        rules.put('э','е');
    }

    private static final Random random = new Random();

    private static int countVowels(String text) {
        if (text != null) {
            String vwls = text.replaceAll( "(?i)[^аеёиоуыэюяАЕЁИОУЫЭЮЯ]+", "" );
            int t = text.length(), l = vwls.length();
            return vwls.length();
        }
        return 0;
    }

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

    public static String text(String text, boolean skipSome) {
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
                        word = word(word, skipSome);
                        res.append(word);
                        word = null;
                    }
                    res.append(cs);
                }
            }
            if (word != null) {
                word = word(word, skipSome);
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
        String lcWord = word.toLowerCase().trim();
        int numVwls = countVowels(lcWord);
        boolean skipSome = numVwls > 2 && random.nextBoolean();
        return word(word, skipSome);
    }

    public static String word(String word, boolean skipSome) {
        String source = word;

        // short
        if (word != null && word.length() >= MIN_SHORT_WORD_LENGTH && word.length() <= MAX_SHORT_WORD_LENGTH) {
            if (!vowels.contains(word.substring(0, 1))) {
                return SHORT_WORD_PRE_1 + word;
            }
        }

        if (word == null || word.length() < MIN_WORD_LENGTH) {
            return word;
        }

        int numVwls = countVowels(word);
        if (numVwls < 2 && word.length() < MIN_WORD_LENGTH_1V) {
            return word;
        }

        word = word.trim();
        Boolean isUcWord = StringUtils.isAllUpperCase(""+word.charAt(0));
        int wcCnt = 0;

        for (char letter : word.toCharArray()) {
            boolean isUc = Character.isUpperCase(letter);
            char lcLetter = Character.toLowerCase(letter);
            if (vowels.indexOf(lcLetter) != -1) {
                if (!skipSome) {
                    if (rules.containsKey(lcLetter)) {
                        word = (isUc ? Character.toUpperCase(rules.get(lcLetter)) : rules.get(lcLetter)) + word.substring(1);
                    }
                    break;
                } else {
                    skipSome = false;
                    word = word.substring(1);
                }
            } else {
                word = word.substring(1);
            }
            wcCnt++;
        }
        return word.isEmpty() ? source : ((isUcWord?"Х":"х") + "у" + word);
    }

    @Test
    public void doTests() {

        /*
        String S = "Привет, ты и дела, и еще Тектоническая плита";
        int t = countVowels(S);
        t = countVowels("Тектоническая");
        t = countVowels("ПЛИТА");
        System.out.println(text(S));

        System.out.println(text("Тектоническая", true));
        System.out.println(text("Тектоническая", false));
        */
        System.out.println(text("майский"));
        if (true) return;

        System.out.println(text("в ходе прокурорских проверок возбуждено 10 уголовных дел о коррупции"));
        System.out.println(text("я хотел купить и спрашивал: а разговаривать по ней можно"));
        System.out.println(text("латынина обнищала совсем, плачется, грозится закрыть передачу"));
        System.out.println("----");
        System.out.println(text("в ходе прокурорских проверок возбуждено 10 уголовных дел о коррупции"));
        System.out.println(text("я хотел купить и спрашивал: а разговаривать по ней можно"));
        System.out.println(text("латынина обнищала совсем, плачется, грозится закрыть передачу"));
    }

    public static void main(String[] args) {
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
            new Qify().doTests();
        }
    }
}
