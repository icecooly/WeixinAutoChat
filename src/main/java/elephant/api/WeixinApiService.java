package elephant.api;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import elephant.Config;
import elephant.api.domain.BatchGetContactResp;
import elephant.api.domain.GetContactResp;
import elephant.api.domain.SendMsgResp;
import elephant.api.domain.SyncCheckResp;
import elephant.api.domain.UpdateChatRoomResp;
import elephant.api.domain.UploadmediaResp;
import elephant.api.domain.WebWxStatusNotifyResp;
import elephant.api.domain.WxInitResp;
import elephant.api.domain.WxSyncResp;
import elephant.utils.JSONUtil;
import elephant.utils.StringUtil;
import elephant.utils.Utils;
import elephant.utils.WxCookieJar;
import io.itit.itf.okhttp.FastHttpClient;
import io.itit.itf.okhttp.HttpClient;
import io.itit.itf.okhttp.PostRequest;
import io.itit.itf.okhttp.Response;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

/**
 * 
 * @author skydu
 *
 */
public class WeixinApiService {
	//
	private static final Logger logger=LoggerFactory.getLogger(WeixinApiService.class);
	//
	public static final String API_URL="https://login.weixin.qq.com";
	public String wxRootURL;//wx2.qq.com or wx.qq.com
	public String wxURL;//
	//
	public static WxCookieJar cookieJar=new WxCookieJar();
	//
	private HttpClient httpClient;
	//
	public static String LANG="zh_CN";
	//
	public WeixinApiService(){
		httpClient=FastHttpClient.newBuilder().
				readTimeout(120, TimeUnit.SECONDS).
				connectTimeout(120, TimeUnit.SECONDS).
				cookieJar(cookieJar).build();
	}
	//
	public String getUUID() throws Exception{
		Response response = httpClient.get().
				url(API_URL+"/jslogin").
				addParams("appid", "wx782c26e4c19acffb").
				addParams("fun", "new").
				addParams("lang", LANG).
				addParams("_", String.valueOf(System.currentTimeMillis())).
				build().
				execute();
		String rsp=response.string();
		String uuid=rsp.substring(rsp.indexOf('"')+1, rsp.lastIndexOf('"'));
		return uuid;
	}
	
	/**
	 * 
	 * @return
	 * @throws Exception 
	 */
	public byte[] getQRCode(String uuid) throws Exception{
		Response response =FastHttpClient.
				get().
				url(API_URL+"/qrcode/"+uuid).
				addHeader("User-Agent", Config.USER_AGENT).
				build().
				execute();
		return response.bytes();	
	}
	
	/**
	 * 
	 * @param uuid
	 * @param tip
	 * @return
	 * @throws Exception
	 */
	public String login(String uuid,String tip,String func) throws Exception{
		String rspMsg=null;
		String url=API_URL+"/cgi-bin/mmwebwx-bin/login";
		long now=System.currentTimeMillis();
		rspMsg=httpClient.get().
				url(url).
				addParams("uuid",uuid).
				addParams("tip", tip).
				addParams("loginicon", "true").
				addParams("R",getR()).
				addParams("_", String.valueOf(now)).
				build().
				execute().
				string();
		//
		if(rspMsg.indexOf("window.code=200")!=-1){//success
			String[] content=rspMsg.split("\n");
			String regex="window.redirect_uri=\"(\\S+)\";";
			String redirectUri= Utils.getMatchGroup0(regex,content[1])+"";
			String tmpUrl=redirectUri.replaceAll("https://", "");
			wxRootURL=tmpUrl.substring(0,tmpUrl.indexOf("/")).trim();
			wxURL=redirectUri.substring(0, redirectUri.lastIndexOf("/"));
			//
			String xml=FastHttpClient.newBuilder().
					cookieJar(cookieJar).
					followRedirects(false).
					followSslRedirects(false).
					build().
					get().
					url(redirectUri).
					build().
					execute().
					string();//login success
			if(logger.isDebugEnabled()){
				logger.debug("redirectUri:{} wxRootURL:{} wxURL:{} tmpUrl:{} xml:{}",redirectUri,wxRootURL,wxURL,xml);
			}
			return xml;
		}
		throw new IllegalArgumentException(rspMsg);
	}
	
	public String getR(){
		return System.currentTimeMillis()+"";
	}
	
	public String getRr(){
		int now=(int) System.currentTimeMillis();
		return (~now)+"";
	}
	
	//
	public WxInitResp wxInit(String body) throws Exception{
		String url=wxURL+"/webwxinit?r="+getRr();
		return post(url, body, WxInitResp.class);
	}
	
	//
	public GetContactResp getContact(String passTicket,String skey,String body) throws Exception{
		String url=String.format("%s/webwxgetcontact?seq=0&r=%s&skey=%s",
				wxURL,
				getR(),
				skey);
		return post(url, body, GetContactResp.class);
	}
	
	//
	public BatchGetContactResp batchGetContact(String body) throws Exception{
		String url=String.format("%s/webwxbatchgetcontact?type=%s&r=%s",
				wxURL,
				"ex",
				getR());
		return post(url, body,BatchGetContactResp.class);
	}
	
	//
	public WebWxStatusNotifyResp wxSatusNotify(String body) throws Exception{
		String url=wxURL+"/webwxstatusnotify";
		return post(url, body, WebWxStatusNotifyResp.class);
	}
	//
	/**
	 * 心跳包，与服务器同步并获取状态
	 * @param sid
	 * @param skey
	 * @param passTicket
	 * @param syncKey
	 * @param deviceId
	 * @throws Exception
	 */
	public SyncCheckResp syncCheck(String sid,String skey,String uin,String passTicket,
			String syncKey,String deviceId) 
	throws Exception{
		SyncCheckResp rsp=null;
		Map<String,String> params=new HashMap<>();
		params.put("uin", uin);
		params.put("sid", urlEncode(sid));
		params.put("skey", urlEncode(skey));
		params.put("r", String.valueOf(System.currentTimeMillis()));
		params.put("synckey", urlEncode(syncKey));
		params.put("deviceid", "e"+StringUtil.randomNumbers(15));
		params.put("_", String.valueOf(System.currentTimeMillis()));
		String url=String.format("https://webpush.%s/cgi-bin/mmwebwx-bin/synccheck",wxRootURL);
		try {
			String json=FastHttpClient.newBuilder().
				connectTimeout(30, TimeUnit.SECONDS).
				readTimeout(30, TimeUnit.SECONDS).
				writeTimeout(30, TimeUnit.SECONDS).
				followSslRedirects(true).
				build().
				get().
				url(url).
				addHeader("cookie", WxCookieJar.cookieHeader()).
				params(params).
				addHeader("User-Agent", Config.USER_AGENT).
				build().
				execute().
				string();
			if(logger.isInfoEnabled()){
				logger.info("syncCheck result:{}",json);
			}
			json=json.replace("window.synccheck=","").
					replace("retcode", "\"retcode\"").
					replace("selector", "\"selector\"");
			rsp=JSONUtil.fromJson(json, SyncCheckResp.class);
		} finally {
			if(logger.isDebugEnabled()){
				logger.debug("syncCheck \nurl:{} sid:{} skey:{} syncKey:{} params:{} cookies:{}"
						+ "\njson:{} ",
						url,sid,skey,syncKey,JSONUtil.dump(params),
						WxCookieJar.cookieHeader(),JSONUtil.dump(rsp));
			}
		}
		return rsp;
	}
	//
	public String urlEncode(String data) throws UnsupportedEncodingException{
		if(data==null){
			return null;
		}
		return URLEncoder.encode(data,"utf8");
	}
	//
	public WxSyncResp wxSync(String sid,String skey,String passTicket,String body) throws Exception{
		String url=String.format(wxURL+"/webwxsync?sid=%s&skey=%s",
				urlEncode(sid),
				urlEncode(skey));
		return post(url, body, WxSyncResp.class);
	}
	
	//发送文本消息
	public SendMsgResp sendMsg(String passTicket,String body) throws Exception{
		String url=String.format(wxURL+"/webwxsendmsg?pass_ticket=%s&r=%s&lang=%s",
				passTicket,System.currentTimeMillis()+"",LANG);
		return post(url,body,SendMsgResp.class);
	}
	
	//发送图片(测试ok)
	public SendMsgResp sendImgMsg(String body) throws Exception{
		String url=String.format(wxURL+"/webwxsendmsgimg?fun=async&f=json");
		return post(url,body,SendMsgResp.class);
	}
	//
	
	//撤回消息
	public void revokeMsg(String passTicket,String body) throws Exception{
		String url=String.format(wxURL+"/webwxrevokemsg?pass_ticket=%s&r=%s",
				passTicket,
				getR());
		post(url,body);
	}
	
	//同意加为好友
	public void verifyUser(String passTicket,String body) throws Exception{
		String url=String.format(wxURL+"/webwxverifyuser?"+
				"pass_ticket=%s&r=%s&lang=%s",
				passTicket,
				System.currentTimeMillis()+"",
				LANG);
		post(url, body);
	}
	
	//创建群组
	public void createChatRoom(String body) throws Exception{
		String url=String.format(wxURL+"/webwxcreatechatroom?r=%s",
				System.currentTimeMillis()+"");
		post(url, body);
	}
	
	public UpdateChatRoomResp updateChatRoom4AddMember(String passTicket,String body) 
	throws Exception{
		String url=String.format(wxURL+"/webwxupdatechatroom?fun=%s&lang=%s&pass_ticket=%s",
				"addmember",
				LANG,
				urlEncode(passTicket));
		return post(url,body,UpdateChatRoomResp.class);
	}
	
	//上传图片
	@SuppressWarnings("deprecation")
	public UploadmediaResp uploadMedia(String body,String fileName,byte[] content) throws Exception{
		UploadmediaResp rsp=null;
		String url=String.format("https://file.%s/cgi-bin/mmwebwx-bin/webwxuploadmedia?f=json",wxRootURL);
		try {
			MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
			builder.addFormDataPart("id","WU_FILE_1");
			builder.addFormDataPart("name","image.png");
			builder.addFormDataPart("type","image/png");
			builder.addFormDataPart("lastModifiedDate",new Date().toLocaleString());
			builder.addFormDataPart("mediatype","pic");
			builder.addFormDataPart("uploadmediarequest",body);
			RequestBody fileBody = RequestBody.create(MediaType.parse(PostRequest.getMimeType(fileName)),
					content);
			builder.addFormDataPart("filename","image.png",fileBody);
			MultipartBody multipartBody=builder.build();
			String json=httpClient.post().
					url(url).
					addHeader("User-Agent", Config.USER_AGENT).
					addHeader("Accept-Encoding","gzip, deflate").
					addHeader("Referer","https://"+wxRootURL+"/").
					multipartBody(multipartBody).
					build().execute().string();
			rsp=JSONUtil.fromJson(json, UploadmediaResp.class);
			if(rsp.BaseResponse.Ret!=0){
				throw new IllegalArgumentException("参数错误");
			}
			return rsp;
		} finally {
			if(logger.isDebugEnabled()){
				logger.debug("createChatroom url:{} body:{} json:{}",
						url,body,JSONUtil.dump(rsp));
			}
		}
	}
	
	public byte[] getHeadimg(String headImg) throws Exception{
		String url=String.format("%s%s","https://%s",headImg,wxRootURL);
		logger.info("getHeadimg url:{}",url);
		return httpClient.get().url(url).build().execute().bytes();
	}
	
	//
	private void post(String url,String body) throws Exception{
		post(url, body, null);
	}
	
	//
	private <T> T post(String url,String body,Class<?> clazz) throws Exception{
		String json=null;
		try {
			json=httpClient.post().
					url(url).
					addHeader("Content-type", "application/json; charset=utf-8").
					addHeader("User-Agent", Config.USER_AGENT).
					body(body).
					build().
					execute().
					string();
			if(clazz==null){
				return null;
			}
			return JSONUtil.fromJson(json, clazz);
		} finally {
			if(logger.isDebugEnabled()){
				logger.debug("post url:{} body:{} json:{}",url,body,json);
			}
		}
	}
}
