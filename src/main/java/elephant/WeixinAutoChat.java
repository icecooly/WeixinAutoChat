package elephant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import elephant.api.WeixinApiService;
import elephant.api.domain.BaseRequest;
import elephant.api.domain.BatchGetContactResp;
import elephant.api.domain.Contact;
import elephant.api.domain.GetContactResp;
import elephant.api.domain.Message;
import elephant.api.domain.SendMsg;
import elephant.api.domain.SyncCheckResp;
import elephant.api.domain.SyncKey;
import elephant.api.domain.SyncKey.KeyValue;
import elephant.api.domain.UpdateChatRoomResp;
import elephant.api.domain.UploadmediaResp;
import elephant.api.domain.WxInitResp;
import elephant.api.domain.WxSyncResp;
import elephant.threadpool.ThreadPool;
import elephant.utils.FileUtil;
import elephant.utils.JSONUtil;
import elephant.utils.QRCodeUtil;
import elephant.utils.XmlUtil;

/**
 * 
 * @author skydu
 *
 */
public class WeixinAutoChat {
	//
	static{
		System.setProperty ("jsse.enableSNIExtension","false");
	}
	//
	private static final Logger logger=LoggerFactory.getLogger(WeixinAutoChat.class);
	//
	private String uuid;
	public WeixinApiService apiService;
	public BaseRequest baseRequest;
	public WxInitResp wxInitResp;
	public GetContactResp getContactResp;
	public String passTicket;
	public ThreadPool threadPool;
	private WeixinCallback callback;
	private SyncKey syncKey;
	private ScheduledFuture<?> startReceiveFuture;
	private Map<String,Contact> contacts;
	//
	public WeixinAutoChat(WeixinCallback callback){
		apiService=new WeixinApiService();
		this.callback=callback;
		threadPool=new ThreadPool();
		contacts=new ConcurrentHashMap<>();
		threadPool.start();
	}
	//
	public interface WeixinCallback {
        void onLogin();
        void onReceiveMsg(List<Message> msgs);
    }
	//
	public void login() throws Exception{
		try {
			uuid=apiService.getUUID();
			byte[] content=apiService.getQRCode(uuid);
			FileUtil.save(Config.qrCodePath, content);
			QRCodeUtil.showQrcode(Config.qrCodePath);
			threadPool.executeThreadWorker(this::startLoginThread);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			throw new RuntimeException("获取登录二维码失败");
		}
	}
	//
	private void startLoginThread(){
		String loginRsp="";
		logger.debug("1.startLoginThread");
		//check login
		while(true){
			try {
				loginRsp=apiService.login(uuid,"0",null);
				break;
			} catch (Exception e) {
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(),e);
			}
		}
		logger.debug("2.登录成功");
		try {
			Map<String, String> map=XmlUtil.parseXmlMessage(loginRsp);
			baseRequest=new BaseRequest();
			baseRequest.Sid=map.get("wxsid");
			baseRequest.Skey=map.get("skey");
			baseRequest.Uin=map.get("wxuin");
			passTicket=map.get("pass_ticket");
			wxInitResp=apiService.wxInit(getBaseRequestParas());
			wxInitResp.ContactList.forEach((c)->contacts.put(c.UserName, c));
			logger.debug("3.初始化成功");
			syncKey=wxInitResp.SyncKey;
			getContactResp=apiService.getContact(passTicket,baseRequest.Skey,getBaseRequestParas());
			getContactResp.MemberList.forEach((c)->contacts.put(c.UserName, c));
			logger.debug("4.获取联系人列表成功");
			apiService.wxSatusNotify(getWxSatusNotifyBody());
			//
			logger.debug("5.开启微信状态通知成功");
			//
			startReceiveFuture=threadPool.scheduleAtFixedRate(this::startReceive, 1, 5, TimeUnit.SECONDS);
			//
			if(callback!=null){
				callback.onLogin();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
	}
	//
	private Map<String,Object> createBody(){
		Map<String,Object> map=new HashMap<>();
		baseRequest.randomDeviceID();
		map.put("BaseRequest",baseRequest);
		return map;
	}
	//
	public void batchGetContact(List<String> chatRoomUserNames) throws Exception{
		Map<String,Object> map=createBody();
		map.put("Count", chatRoomUserNames.size());
		List<Map<String,String>> list=new ArrayList<>();
		for (String userName:chatRoomUserNames) {
			Map<String,String> member=new HashMap<>();
			member.put("UserName",userName);
			member.put("EncryChatRoomId", "");
			list.add(member);
		}
		map.put("List", list);
		BatchGetContactResp resp=apiService.batchGetContact(JSONUtil.toJson(map));
		if(resp.BaseResponse.Ret==0&&resp.ContactList!=null){
			resp.ContactList.forEach((c)->contacts.put(c.UserName, c));
		}
		
	}
	//
	private String getWxSatusNotifyBody(){
		Map<String,Object> map=createBody();
		map.put("Code", 3);
		map.put("FromUserName", wxInitResp.User.UserName);
		map.put("ToUserName", wxInitResp.User.UserName);
		map.put("ClientMsgId", System.currentTimeMillis()+"");
		return JSONUtil.toJson(map);
	}
	//
	private String getBaseRequestParas(){
		Map<String,Object> map=new HashMap<>();
		map.put("BaseRequest",baseRequest);
		return JSONUtil.toJson(map);
	}
	//
	private void startReceive(){
		Map<String,Object> map=createBody();
		map.put("SyncKey", wxInitResp.SyncKey);
		map.put("rr", apiService.getRr());
		try {
			SyncCheckResp resp=apiService.syncCheck(baseRequest.Sid,baseRequest.Skey,baseRequest.Uin,
					passTicket,createSyncKey(syncKey),baseRequest.DeviceID);
			if(resp.retcode!=SyncCheckResp.RETCODE_正常){
				if(resp.retcode==SyncCheckResp.RETCODE_退出){
					logger.info("退出");
					startReceiveFuture.cancel(true);
				}else if(resp.retcode==SyncCheckResp.RETCODE_移动端退出){
					logger.info("移动端退出");
					startReceiveFuture.cancel(true);
				}else if(resp.retcode==SyncCheckResp.RETCODE_其它地方登陆){
					logger.info("其它地方登陆");
					startReceiveFuture.cancel(true);
				}else if(resp.retcode==SyncCheckResp.RETCODE_未知错误){
					logger.info("未知错误");
				}
				return;
			}else{
				if(resp.selector!=0){//说明有新消息
					WxSyncResp syncResp=apiService.wxSync(baseRequest.Sid, baseRequest.Skey,
							passTicket,JSONUtil.toJson(map));
					if (resp.selector==2) {//新消息
						callback.onReceiveMsg(syncResp.AddMsgList);
					}
					syncKey=syncResp.SyncCheckKey;;
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
	}
	//
	private String createSyncKey(SyncKey key) throws Exception{
		StringBuilder skey=new StringBuilder();
		for (KeyValue kv : key.List) {
			skey.append(kv.Key).append("_").append(kv.Val).append("|");
		}
		skey.deleteCharAt(skey.length()-1);
		return skey.toString();
	}
	//
	public void sendMsg(SendMsg msg) throws Exception{
		Map<String,Object> map=createBody();
		msg.FromUserName=wxInitResp.User.UserName;
		map.put("Msg", msg);
		map.put("Scene", 0);
		if(msg.Type==Message.TYPE_文本消息){
			apiService.sendMsg(passTicket,JSONUtil.toJson(map));
		}else if(msg.Type==Message.TYPE_图片消息){
			apiService.sendImgMsg(JSONUtil.toJson(map));
		}
	}
	//
	public void revokeMsg(String msgId,String toUserName) throws Exception{
		Map<String,Object> map=createBody();
		map.put("SvrMsgId", msgId);
		map.put("ToUserName", toUserName);
		map.put("ClientMsgId", System.currentTimeMillis());
		apiService.revokeMsg(passTicket,JSONUtil.toJson(map));
		logger.debug("revokeMsg msg:{}",JSONUtil.dump(map));
	}
	//
	public void verifyUser(String fromUserName,String verifyUserTicket) throws Exception{
		Map<String,Object> map=createBody();
		map.put("Opcode", 3);
		map.put("VerifyUserListSize", 1);
		Map<String,String> VerifyUserList=new HashMap<>();
		VerifyUserList.put("Value", fromUserName);
		VerifyUserList.put("VerifyUserTicket", verifyUserTicket);
		map.put("VerifyUserList",Arrays.asList(VerifyUserList));
		map.put("VerifyContent","");
		map.put("SceneListCount",1);
		map.put("SceneList",Arrays.asList(33));
		map.put("skey",baseRequest.Skey);
		apiService.verifyUser(passTicket,JSONUtil.toJson(map));
	}
	//
	public void createChatRoom(String topic,List<Contact> contacts) throws Exception{
		if(contacts==null||contacts.size()<2){
			throw new IllegalAccessException("参数错误");
		}
		Map<String,Object> map=createBody();
		map.put("MemberCount", contacts.size());
		List<Map<String,String>> contactList=new ArrayList<>();
		for (Contact c:contacts) {
			Map<String,String> contact=new HashMap<>();
			contact.put("UserName", c.UserName);
			contactList.add(contact);
		}
		map.put("MemberList", contactList);
		map.put("Topic",topic);
		apiService.createChatRoom(JSONUtil.toJson(map));
	}
	
	//群组添加成员
	public UpdateChatRoomResp updateChatRoom4AddMember(List<String> addMemberList,String chatRoomName) 
	throws Exception{
		Map<String,Object> map=createBody();
		StringBuilder memberList=new StringBuilder();
		for (String member : addMemberList) {
			memberList.append(member).append(",");
		}
		memberList.deleteCharAt(memberList.length()-1);
		map.put("AddMemberList",memberList);
		map.put("ChatRoomName", chatRoomName);
		return apiService.updateChatRoom4AddMember( passTicket, JSONUtil.toJson(map));
	}
	
	//发送图片
	public void sendImgMsg(String toUserName,String fileName,byte[] content) throws Exception{
		Contact to=getContactByUserName(toUserName);
		if(to==null){
			throw new IllegalArgumentException("用户不存在"+toUserName);
		}
		UploadmediaResp resp=uploadMedia(toUserName, fileName, content);
		//
		SendMsg msg=new SendMsg();
		msg.Type=Message.TYPE_图片消息;
		msg.Content="";
		msg.MediaId=resp.MediaId;
		msg.FromUserName=wxInitResp.User.UserName;
		msg.ToUserName=to.UserName;
		sendMsg(msg);
	}
	
	//
	public UploadmediaResp uploadMedia(String toUserName,String fileName,byte[] content) throws Exception{
		if(content==null||content.length<0){
			throw new IllegalAccessException("参数错误");
		}
		Map<String,Object> map=createBody();
		map.put("UploadType", 2);
		map.put("ClientMediaId", System.currentTimeMillis());
		map.put("TotalLen",content.length);
		map.put("StartPos",0);
		map.put("DataLen",content.length);
		map.put("MediaType",4);
		map.put("FromUserName",wxInitResp.User.UserName);
		map.put("ToUserName",toUserName);
		return apiService.uploadMedia(JSONUtil.toJson(map),fileName,content);
	}
	
	//
	public Contact getContactsByNickName(String nickName){
		for (Contact contact : getContactResp.MemberList) {
			if(contact.NickName.equals(nickName)){
				return contact;
			}
		}
		return null;
	}
	//
	public Contact getContactByUserName(String userName){
		return contacts.get(userName);
	}
	//
	public Contact getChatRoomByNickName(String nickName){
		for (Contact contact : contacts.values()) {
			if(contact.UserName.startsWith("@@")&&
					contact.NickName.equals(nickName)){
				return contact;
			}
		}
		return null;
	}
	//
	public void sendTextMsg(String toUserName,String text) throws Exception{
		SendMsg msg=new SendMsg();
		msg.Type=Message.TYPE_文本消息;
		msg.Content=text;
		msg.FromUserName=wxInitResp.User.UserName;
		msg.ToUserName=toUserName;
		sendMsg(msg);
	}
	
	public byte[] getHeadimg(String headImg) throws Exception{
		return apiService.getHeadimg(headImg);
	}
	
	public Collection<Contact> getContacts(){
		return contacts.values();
	}
	
	public Contact getMySelf(){
		if(wxInitResp==null){
			return null;
		}
		return wxInitResp.User;
	}
}
