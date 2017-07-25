package elephant.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author skydu
 *
 */
public class Utils {

	/**
	 * 
	 * @param regex
	 * @param text
	 * @return
	 */
	public static Matcher getMatcher(String regex, String text) {
		return Pattern.compile(regex).matcher(text);
	}
	
	/**
	 * 
	 * @param regex
	 * @param text
	 * @return
	 */
	public static String getMatchGroup0(String regex, String text) {
		Matcher m=getMatcher(regex, text);
		if (m.find()) {
			return m.group(1);
		}
		return null;
	}
}
