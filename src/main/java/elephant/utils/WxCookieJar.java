package elephant.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

/**
 * 
 * @author skydu
 *
 */
public class WxCookieJar implements CookieJar {
	//
	private static Map<String,Cookie> cookieMap=new ConcurrentHashMap<>();
	//
	@Override
	public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
		if(cookies!=null){
			for (Cookie cookie : cookies) {
				cookieMap.put(cookie.name(), cookie);
			}
		}
	}

	@Override
	public List<Cookie> loadForRequest(HttpUrl url) {
		return new ArrayList<Cookie>(cookieMap.values());
	}
	
	public static String cookieHeader() {
		StringBuilder cookieHeader = new StringBuilder();
		for (Cookie cookie :cookieMap.values()) {
	      cookieHeader.append(cookie.name()).append('=').append(cookie.value());  
	      cookieHeader.append("; ");
		}
	    return cookieHeader.toString();
	  }
}
