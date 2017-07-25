package elephant.api.domain;

import java.util.List;

/**
 * 
 * @author skydu
 *
 */
public class WxInitResp {

	public BaseResponse BaseResponse;
	
	public int Count;
	
	public List<Contact> ContactList;
	
	public SyncKey SyncKey;
	
	public Contact User;//自己
}
