package elephant.api.domain;

import elephant.utils.RandomUtil;

/**
 * 
 * @author skydu
 *
 */
public class SendMsg {

	public int Type;//1 文字消息
	
	public String Content;//要发送的消息
	
	public String FromUserName;//自己的ID
	
	public String ToUserName;//好友的ID
	
	public String LocalID;//与clientMsgId相同
	
	public String ClientMsgId;//时间戳左移4位随后补上4位随机数
	
	public String MediaId;
	
	public SendMsg(){
		LocalID=System.currentTimeMillis()+""+RandomUtil.randomInt(1000, 9999);
		ClientMsgId=LocalID;
	}
}
