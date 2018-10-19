package irc.tula.tg.core;


import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

@Deprecated
@Slf4j
public class MatchWild {
	public static final String preparePattern(String str) {
		try {
			//String res = StringUtils.normalizeSpace(str);
			String[] parts = str.split(" ");
			StringBuilder res = new StringBuilder();
			for (String i : parts) {
				if (StringUtils.isNotBlank(i)) {
					if (res.length() > 0) {
						res.append("+");
					}
					res.append(i);
				}
			}
			return res.toString();
		} catch (Exception ex) {
			log.error("preparePattern: {}", ex);
			return str;
		}
	}

	public static final int match_wild(String pattern, String str) {
		int pattern_index = 0;
		int str_index = 0;
		char c;

		for (; ; ) {
			if (pattern_index >= pattern.length()) {
				if (str_index > str.length()) {
					return 1;
				}
				return 0;
			}

			c = pattern.charAt(pattern_index);
			pattern_index++;

			switch (c) {
				case '?':
					str_index++;
					break;
				case '*':
					if (pattern_index >= pattern.length()) {
						return 1;
					}
					int s_index = 0;
					String s = str.substring(str_index);

					while (s_index < s.length()) {
						if (s.charAt(s_index) == pattern.charAt(pattern_index)) {
							String p = pattern.substring(pattern_index);
							s = str.substring(str_index+s_index);
							 if (match_wild(p, s) != 0) {
								 return 1;
							 }
						}
						s_index++;
					}
					break;
				default:
					if (str_index < str.length()) {
						if (str.charAt(str_index) != c) {
							//if (str_index++ < str.length() && str.charAt(str_index) != c) {
							return 0;
						}
						str_index++;
					} else {
						return 0;
					}
					break;
			}
		}
	}

	public static void T(String p, String t) {
		System.out.println("T \'" + p + "\', \'" + t);
		int r = MatchWild.match_wild(p, t);
		System.out.println("" + r);
	}
	public static void main(String[] args) {
		T("a*k*o", preparePattern("а кто вернулся в село а там опять яблокоболь"));
		T("a*kto?", preparePattern("a kto"));
		T("a*k*o", preparePattern(""));

		T("передает*привет!*", preparePattern("пе"));
		//T("передает*привет*", preparePattern("а кто в жопе"));

		/*
		T("a*", "a+kto");
		T("a*k*", "a+kto");
		T("a*k*o", "a+kto");
		T("a*kto", "a+kto");
		T("a*kto", "a+ktto");
		T("a*kto*", "a+kto+заебался?");
		*/
	}
}

