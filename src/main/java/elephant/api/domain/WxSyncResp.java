package elephant.api.domain;

import java.util.List;

/**
 * 
 * @author skydu
 *
 */
public class WxSyncResp {

	public BaseResponse BaseResponse;
	
	public int AddMsgCount;
	
	public List<Message> AddMsgList;
	
	public int ModContactCount;//变更联系人数量
	
	public int DelContactCount;//删除联系人数量
	
	public List<String> DelContactList;//TODO
	
	public int ModChatRoomMemberCount;//
	
	public List<String> ModChatRoomMemberList;//TODO
	
	public Contact User;
	
	public int ContinueFlag;
	
	public SyncKey SyncKey;
	
	public String SKey;
	
	public SyncKey SyncCheckKey;
}
