package elephant;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import elephant.utils.JSONUtil;
import io.itit.itf.okhttp.FastHttpClient;

/**
 * 
 * @author skydu
 *
 */
public class AutoReplyAI {
	//
	private static Logger logger=LoggerFactory.getLogger(AutoReplyAI.class);
	//
	private static final String TOKEN="97AAF6D2AEE22DC53F4C90D8B1BD7348";
	private static AtomicLong sessionId=new AtomicLong(0);
	//
	public static String query(String query){
		String json=null;
		try {
			if(query!=null&&query.length()>0) {
				if(!query.endsWith("？")) {
					query+="？";
				}
			}
			json=FastHttpClient.post().url("http://www.yige.ai/v1/query").
					addParams("token",TOKEN).
					addParams("reset_state","1").
					addParams("session_id",""+sessionId.incrementAndGet()).
					addParams("query", query).build().execute().string();
			AIQueryResp resp=JSONUtil.fromJson(json, AIQueryResp.class);
			if(!resp.status.code.equals("200")) {
				return "[微笑]您说的我不明白";
			}
			return resp.answer;
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
			return "[微笑]您说的我不明白";
		} 
		finally {
			if(logger.isDebugEnabled()) {
				logger.debug("query {} json:{}",query,json);
			}
		}	
	}
	//
	public static void main(String[] args) throws IOException, Exception {
		System.out.println(AutoReplyAI.query("你多大"));
	}
}
