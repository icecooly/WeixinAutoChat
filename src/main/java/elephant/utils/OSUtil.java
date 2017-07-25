package elephant.utils;
/**
 * 
 * @author skydu
 *
 */
public class OSUtil {
	//
	private static String OS = System.getProperty("os.name").toLowerCase();
	//
	public static boolean isWindows() {
		return OS.indexOf("windows") >= 0;
	}
	//
	public static boolean isLinux() {
		return OS.indexOf("linux") >= 0;
	}
	//
	public static boolean isMacOS() {
		return OS.indexOf("mac") >= 0 && OS.indexOf("os") > 0;
	}
}
