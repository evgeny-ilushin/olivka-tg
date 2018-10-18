package irc.tula.tg.core;


public class MatchWild {
	public static final int match_wild(String pattern, String str) {
		int pattern_index = 0;
		int str_index = 0;
		char c = 0;

		for (; ; ) {
			c = pattern.charAt(pattern_index);
			pattern = pattern.substring(pattern_index);

			switch (c) {
			/*
			case '\x000':
				if (str.length() < 1)
				{
					return 1;
				}
				return 0;
			*/
				case '?':
					str_index++;
					str = str.substring(str_index);
					break;
				case '*':
					if (pattern.length() <= 0) {
						return 1;
					}
					int s_index = 0;
					String s = str.substring(str_index);
					while (s.length() > 0) {
						if (s.length() > 0 && str.charAt(s_index) == pattern.charAt(pattern_index) && match_wild(pattern, s) != 0) {
							return 1;
						}
						s_index++;
						s = str.substring(s_index);
					}
					break;
				default:
					if (str.charAt(str_index++) != c) {
						return 0;
					}
					break;
			}
		}
	}

	public static void T(String p, String t) {
		int r = MatchWild.match_wild("*", "12312312");
		System.out.println("T \'" + p + "\', \'" + t + "\': " + r);
	}
	public static void main(String[] args) {
		T("*", "12312312");
		T("*", "1231231");
		T("??", "1231231");
	}
}

