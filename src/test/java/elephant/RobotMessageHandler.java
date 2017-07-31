package elephant;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import elephant.WeixinAutoChat.WeixinCallback;
import elephant.api.domain.Contact;
import elephant.api.domain.Member;
import elephant.api.domain.Message;
import elephant.utils.JSONUtil;

/**
 * 微信机器人
 * @author skydu
 *
 */
public class RobotMessageHandler implements WeixinCallback{
	//
	private static Logger logger=LoggerFactory.getLogger(RobotMessageHandler.class);
	//
	public WeixinAutoChat chat;
	//
	public Map<String,Message> receiveMessages=new ConcurrentHashMap<>();
	//
	public static final String CHAT_ROOM_PASSWORD="999999";
	public static final String CHAT_ROOM_NAME="wxjava";
	//
	private String chatRoomUserName;
	//
	@Override
	public void onLogin() {
		logger.debug("onLogin");
	}
	//添加到群里
	private void addMember4ChatRoom(String userName,String chatRoomNickName){
		try{
			if(logger.isDebugEnabled()){
				logger.debug("addMember4ChatRoom userName:{} chatRoomNickName:{}",userName,chatRoomNickName);
			}
			Contact chatRoom=chat.getChatRoomByNickName(chatRoomNickName);
			if(chatRoom!=null){
				chatRoomUserName=chatRoom.UserName;
				chat.batchGetContact(Arrays.asList(chatRoom.UserName));
				chatRoom=chat.getChatRoomByNickName(chatRoomNickName);
				for (Member member:chatRoom.MemberList) {
					if(member.UserName.equals(userName)){
						logger.debug("is already chat room member {}",userName);
						return;
					}
				}
				chat.updateChatRoom4AddMember(Arrays.asList(userName),chatRoom.UserName);
				chat.batchGetContact(Arrays.asList(chatRoom.UserName));
				chatRoom=chat.getChatRoomByNickName(chatRoomNickName);
				for (Member m:chatRoom.MemberList) {
					if(m.UserName.equals(userName)){
						chat.sendTextMsg(chatRoom.UserName, "[玫瑰]欢迎光临"+m.NickName);
						break;
					}
				}
			}else{
				logger.error("chat romm not found.chatRoomNickName:{}",chatRoomNickName);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	//
	public static byte[] toByteArray(InputStream input) throws IOException {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		for (int n = input.read(buf); n != -1; n = input.read(buf)) {
			os.write(buf, 0, n);
		}
		return os.toByteArray();
	}
	//
	private void replyMsg(String toUserName,String fromMsg) throws Exception {
		if(fromMsg.indexOf("你的国籍是")!=-1) {
			//发图片
			InputStream is=this.getClass().getResourceAsStream("chinagq.png");
			chat.sendImgMsg(toUserName, "image.png", toByteArray(is));
		}else {
			chat.sendTextMsg(toUserName,AutoReplyAI.query(fromMsg));
		}
	}
	//
	@Override
	public void onReceiveMsg(List<Message> msgs) {
		logger.info("onReceiveMsg msgs size:{}",msgs.size());
		//
		for (Message msg : msgs) {
			if(logger.isDebugEnabled()){
				logger.debug("new msg:{}",JSONUtil.dump(msg));
			}
			if(receiveMessages.containsKey(msg.MsgId)){
				logger.debug("save message MsgId:{}",msg.MsgId);
				continue;
			}
			try {
				if(msg.MsgType==Message.TYPE_文本消息){
					if(msg.ToUserName.equals(chat.getMySelf().UserName)){//私聊消息
						if(msg.Content.equals(CHAT_ROOM_PASSWORD)){
							chat.sendTextMsg(msg.FromUserName, "[微笑]群口令正确");
							addMember4ChatRoom(msg.FromUserName,CHAT_ROOM_NAME);
						}else{
							replyMsg(msg.FromUserName,msg.Content.trim());
						}
					}
					else if(msg.ToUserName.equals(chatRoomUserName)&&
							(!msg.FromUserName.equals(chat.getMySelf().UserName))) {//别人在wxjava群里说话 @我了
						if(msg.Content.indexOf("@wxjava")!=-1) {
							String content=msg.Content.replaceAll("@wxjava","");
							replyMsg(msg.ToUserName,content.trim());
						}
					}
				}
				if(msg.MsgType==Message.TYPE_好友确认消息){
					logger.debug("收到好友确认消息:{}",JSONUtil.dump(msg));
					chat.verifyUser(msg.RecommendInfo.UserName,msg.RecommendInfo.Ticket);
					chat.sendTextMsg(msg.RecommendInfo.UserName, "[微笑]请您输入"+CHAT_ROOM_NAME+"群口令:");
				}
			} catch (Exception e) {
				logger.error(e.getMessage(),e);
			}
			//
			receiveMessages.put(msg.MsgId, msg);
		}	
	}
}
