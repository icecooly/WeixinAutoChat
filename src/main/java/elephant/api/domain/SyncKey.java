package elephant.api.domain;

import java.util.List;

/**
 * 
 * @author skydu
 *
 */
public class SyncKey {

	public static class KeyValue{
		public String Key;
		public String Val;
	}
	
	public int Count;
	
	public List<KeyValue> List;
}
