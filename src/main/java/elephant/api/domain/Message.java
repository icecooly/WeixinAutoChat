package elephant.api.domain;

/**
 * 
 * @author skydu
 *
 */
public class Message {
	//
	//MsgType	说明
	public static final int TYPE_文本消息=1;	
	public static final int TYPE_图片消息=3;
	public static final int TYPE_语音消息=34;
	public static final int TYPE_好友确认消息=37;
	public static final int TYPE_POSSIBLEFRIEND_MSG=40;
	public static final int TYPE_共享名片=42;
	public static final int TYPE_视频消息=43;
	public static final int TYPE_动画表情=47;
	public static final int TYPE_位置消息=48;
	public static final int TYPE_分享链接=49;
	public static final int TYPE_VOIPMSG=50;
	public static final int TYPE_微信初始化消息=51;
	public static final int TYPE_VOIPNOTIFY=52;
	public static final int TYPE_VOIPINVITE=53;
	public static final int TYPE_小视频=62;
	public static final int TYPE_SYSNOTICE=9999;
	public static final int TYPE_系统消息=10000;
	public static final int TYPE_撤回消息=10002;
	//
	public static final String 表情_微笑="[微笑]";
	public static final String 表情_玫瑰="[玫瑰]";
	public static final String 表情_惊恐="[惊恐]";
	public static final String 表情_坏笑="[坏笑]";
	public static final String 表情_强="[强]";
	//
	public static class RecommendInfo{
		public String UserName;//": "@6ba9f91be84f4e1c3e0695bd535a1720840022cc508d27421bccfb410757b079",
		public String NickName;//": "double",
		public int QQNum;//": 0,
		public String Province;//": "广东",
		public String City;//": "深圳",
		public String Content;//": "我是邓爱鹏",
		public String Signature;//": "这个人很懒，什么都没留下。",
		public String Alias;//": "",
		public String Scene;//": 30,
		public int VerifyFlag;//": 0,
		public long AttrStatus;//": 66661,
		public int Sex;//": 1,
		public String Ticket;//": "v2_41de4624f43e9e9091e9b28167efa3c40f1e29b70451d7e8a6c83a54407e6268b660b67166fd64391533e2b7c7a643647ac3a755ad755e726274d2fec297c062@stranger",
		public int OpCode;//": 2
	}
	//
	public String MsgId;// "6111705386542719545",
	public String FromUserName;//"@531d0e03d5c5df69009dc2581fdeb8f4e23b56f0b17f37a17a84b4c70a01c399",
	public String ToUserName;//  "@531d0e03d5c5df69009dc2581fdeb8f4e23b56f0b17f37a17a84b4c70a01c399",
	public int MsgType;//1 文本
	public String Content;// "啊",
	public int Status;//3,
	public int ImgStatus;//1,
	public int CreateTime;// 1498406756,
	public int VoiceLength;// 0,
	public int PlayLength;// 0,
	public String FileName;// ""
	public String FileSize;//: "",
	public String MediaId;//": "",
	public String Url;//": "",
	public int AppMsgType;
	public int StatusNotifyCode;//": 2,
	public String StatusNotifyUserName;//": "@531d0e03d5c5df69009dc2581fdeb8f4e23b56f0b17f37a17a84b4c70a01c399",
	public RecommendInfo RecommendInfo;
//	,
//	"ForwardFlag": 0,
//	"AppInfo": {
//	"AppID": "",
//	"Type": 0
//	}
//	,
//	"HasProductId": 0,
//	"Ticket": "",
//	"ImgHeight": 0,
//	"ImgWidth": 0,
//	"SubMsgType": 0,
//	"NewMsgId": 6111705386542719545,
//	"OriContent": ""
}
